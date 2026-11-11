package ro.studentassist.buildings.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import ro.studentassist.buildings.dto.*;
import ro.studentassist.buildings.model.Building;
import ro.studentassist.buildings.routing.GeoUtils;
import ro.studentassist.buildings.routing.RoutePattern;
import ro.studentassist.buildings.routing.RoutePatternIndex;
import ro.studentassist.transit.dto.Stop;
import ro.studentassist.transit.dto.TransitRoute;
import ro.studentassist.transit.service.TransitService;
import ro.studentassist.transit.dto.Vehicle;

@Service
@RequiredArgsConstructor
public class BuildingRouteService {

    private static final double MAX_WALK_TO_STOP_METERS = 700;
    private static final double MAX_STOP_TO_SHAPE_METERS = 80;
    private static final double MAX_VEHICLE_TO_SHAPE_METERS = 80;
    private static final double MIN_RIDE_METERS = 200;
    private static final double WALK_DETOUR_FACTOR = 1.3;
    private static final double WALK_METERS_PER_MINUTE = 75;
    private static final double TRANSIT_METERS_PER_MINUTE = 300;
    private static final int ASSUMED_WAIT_MINUTES = 8;
    private static final int MAX_NEARBY_STOPS = 40;
    private static final int MAX_OPTIONS = 3;
    private static final int MAX_PATH_POINTS = 150;

    private final BuildingService buildingService;
    private final TransitService transitService;
    private final RoutePatternIndex routePatternIndex;

    private record StopOnPattern(StopRef stop, int index) {
    }

    private record LiveArrival(int minutes, String vehicleLabel) {
    }

    /** Finds up to three direct transit lines from the given point to the building, fastest first. */
    public BuildingRouteResponse findRoute(Long buildingId, double fromLat, double fromLon) {
        Building building = buildingService.getById(buildingId);

        List<Stop> stops = transitService.getStops().stream()
                .filter(stop -> stop.stopId() != null && stop.stopLat() != null && stop.stopLon() != null)
                .toList();
        List<StopRef> stopsNearOrigin = stopsNear(stops, fromLat, fromLon);
        List<StopRef> stopsNearBuilding = stopsNear(stops, building.getLatitude(), building.getLongitude());

        List<Vehicle> vehicles = transitService.getActiveVehicles();
        Map<Long, TransitRoute> routesById = transitService.getRoutes().stream()
                .filter(route -> route.routeId() != null)
                .collect(Collectors.toMap(TransitRoute::routeId, Function.identity(), (first, second) -> first));

        Set<Long> seenRoutes = new HashSet<>();
        List<RouteOption> options = routePatternIndex.getPatterns().stream()
                .map(pattern -> evaluate(pattern, stopsNearOrigin, stopsNearBuilding, vehicles, routesById))
                .flatMap(Optional::stream)
                .sorted(Comparator.comparingInt(RouteOption::totalMinutes))
                .filter(option -> seenRoutes.add(option.routeId()))
                .limit(MAX_OPTIONS)
                .toList();

        int directDistance = (int) Math.round(
                GeoUtils.distanceMeters(fromLat, fromLon, building.getLatitude(), building.getLongitude()));

        return new BuildingRouteResponse(
                BuildingResponse.from(building),
                new Coordinate(fromLat, fromLon),
                directDistance,
                walkMinutes(directDistance),
                options);
    }

    /** Returns the nearest stops within walking distance of a point, closest first. */
    private List<StopRef> stopsNear(List<Stop> stops, double lat, double lon) {
        return stops.stream()
                .map(stop -> new StopRef(
                        stop.stopId(),
                        stop.stopName(),
                        stop.stopLat(),
                        stop.stopLon(),
                        (int) Math.round(GeoUtils.distanceMeters(lat, lon, stop.stopLat(), stop.stopLon()))))
                .filter(ref -> ref.walkMeters() <= MAX_WALK_TO_STOP_METERS)
                .sorted(Comparator.comparingInt(StopRef::walkMeters))
                .limit(MAX_NEARBY_STOPS)
                .toList();
    }

    /** Builds a route option when the pattern passes near the origin and later near the building. */
    private Optional<RouteOption> evaluate(
            RoutePattern pattern,
            List<StopRef> stopsNearOrigin,
            List<StopRef> stopsNearBuilding,
            List<Vehicle> vehicles,
            Map<Long, TransitRoute> routesById) {

        List<StopOnPattern> boardings = stopsOnPattern(pattern, stopsNearOrigin);
        List<StopOnPattern> alightings = stopsOnPattern(pattern, stopsNearBuilding);

        StopOnPattern bestBoarding = null;
        StopOnPattern bestAlighting = null;
        int bestWalkMeters = Integer.MAX_VALUE;
        for (StopOnPattern boarding : boardings) {
            for (StopOnPattern alighting : alightings) {
                if (alighting.index() <= boarding.index()
                        || pattern.lengthBetween(boarding.index(), alighting.index()) < MIN_RIDE_METERS) {
                    continue;
                }
                int walkMeters = boarding.stop().walkMeters() + alighting.stop().walkMeters();
                if (walkMeters < bestWalkMeters) {
                    bestWalkMeters = walkMeters;
                    bestBoarding = boarding;
                    bestAlighting = alighting;
                }
            }
        }
        if (bestBoarding == null) {
            return Optional.empty();
        }

        int walkToStop = walkMinutes(bestBoarding.stop().walkMeters());
        int walkToBuilding = walkMinutes(bestAlighting.stop().walkMeters());
        double rideMeters = pattern.lengthBetween(bestBoarding.index(), bestAlighting.index());
        int ride = Math.max(1, (int) Math.ceil(rideMeters / TRANSIT_METERS_PER_MINUTE));

        Optional<LiveArrival> live = nextVehicle(pattern, bestBoarding.index(), walkToStop, vehicles);
        int busArrivesIn = live.map(LiveArrival::minutes).orElse(walkToStop + ASSUMED_WAIT_MINUTES);
        int total = busArrivesIn + ride + walkToBuilding;

        TransitRoute route = routesById.get(pattern.routeId());
        return Optional.of(new RouteOption(
                pattern.routeId(),
                route == null ? null : route.routeShortName(),
                route == null ? null : route.routeLongName(),
                route == null ? null : route.routeType(),
                pattern.directionId(),
                pattern.headsign(),
                bestBoarding.stop(),
                bestAlighting.stop(),
                walkToStop,
                busArrivesIn,
                ride,
                walkToBuilding,
                total,
                live.isPresent(),
                live.map(LiveArrival::vehicleLabel).orElse(null),
                path(pattern, bestBoarding.index(), bestAlighting.index())));
    }

    /** Keeps the stops that lie on the pattern shape and records the shape index closest to each. */
    private List<StopOnPattern> stopsOnPattern(RoutePattern pattern, List<StopRef> stops) {
        List<StopOnPattern> result = new ArrayList<>();
        for (StopRef stop : stops) {
            RoutePattern.NearestPoint nearest = pattern.nearest(stop.latitude(), stop.longitude());
            if (nearest.distanceMeters() <= MAX_STOP_TO_SHAPE_METERS) {
                result.add(new StopOnPattern(stop, nearest.index()));
            }
        }
        return result;
    }

    /** Finds the soonest live vehicle on the pattern that has not passed the boarding stop and can still be caught. */
    private Optional<LiveArrival> nextVehicle(
            RoutePattern pattern, int boardingIndex, int walkToStopMinutes, List<Vehicle> vehicles) {

        LiveArrival best = null;
        for (Vehicle vehicle : vehicles) {
            if (!pattern.routeId().equals(vehicle.routeId())
                    || !matchesDirection(vehicle.tripId(), pattern.directionId())) {
                continue;
            }
            RoutePattern.NearestPoint nearest = pattern.nearest(vehicle.latitude(), vehicle.longitude());
            if (nearest.distanceMeters() > MAX_VEHICLE_TO_SHAPE_METERS || nearest.index() > boardingIndex) {
                continue;
            }
            int arrivalMinutes = (int) Math.ceil(
                    pattern.lengthBetween(nearest.index(), boardingIndex) / TRANSIT_METERS_PER_MINUTE);
            if (arrivalMinutes < walkToStopMinutes) {
                continue;
            }
            if (best == null || arrivalMinutes < best.minutes()) {
                best = new LiveArrival(arrivalMinutes, vehicle.label());
            }
        }
        return Optional.ofNullable(best);
    }

    /** Checks that a vehicle trip id of the form routeId_direction ends with the pattern direction. */
    private boolean matchesDirection(String tripId, Integer directionId) {
        return tripId != null && tripId.endsWith("_" + directionId);
    }

    /** Returns the shape coordinates between two indexes, thinned to at most about 150 points. */
    private List<Coordinate> path(RoutePattern pattern, int fromIndex, int toIndex) {
        int count = toIndex - fromIndex + 1;
        int step = Math.max(1, count / MAX_PATH_POINTS);
        List<Coordinate> path = new ArrayList<>();
        for (int i = fromIndex; i <= toIndex; i += step) {
            path.add(new Coordinate(pattern.lats()[i], pattern.lons()[i]));
        }
        if ((toIndex - fromIndex) % step != 0) {
            path.add(new Coordinate(pattern.lats()[toIndex], pattern.lons()[toIndex]));
        }
        return path;
    }

    /** Converts a straight-line walking distance into minutes, adding a detour factor. */
    private int walkMinutes(int meters) {
        return (int) Math.ceil(meters * WALK_DETOUR_FACTOR / WALK_METERS_PER_MINUTE);
    }
}
