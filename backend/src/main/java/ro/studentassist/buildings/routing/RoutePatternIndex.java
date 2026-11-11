package ro.studentassist.buildings.routing;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import ro.studentassist.config.TimedCache;
import ro.studentassist.transit.dto.ShapePoint;
import ro.studentassist.transit.service.TransitService;
import ro.studentassist.transit.dto.Trip;
import ro.studentassist.transit.config.TranzyProperties;

@Component
public class RoutePatternIndex {

    private final TimedCache<List<RoutePattern>> patterns;

    RoutePatternIndex(TransitService transitService, TranzyProperties properties) {
        this.patterns = new TimedCache<>(
                properties.staticCacheTtl(),
                () -> build(transitService.getTrips(), transitService.getShapes()));
    }

    /** Returns one pattern per route, direction and shape, with precomputed distances along the shape. */
    public List<RoutePattern> getPatterns() {
        return patterns.get();
    }

    private static List<RoutePattern> build(List<Trip> trips, Map<String, List<ShapePoint>> shapes) {
        Map<String, Trip> distinctTrips = new LinkedHashMap<>();
        for (Trip trip : trips) {
            if (trip.routeId() == null || trip.directionId() == null || trip.shapeId() == null) {
                continue;
            }
            String key = trip.routeId() + "|" + trip.directionId() + "|" + trip.shapeId();
            distinctTrips.putIfAbsent(key, trip);
        }

        List<RoutePattern> result = new ArrayList<>();
        for (Trip trip : distinctTrips.values()) {
            List<ShapePoint> points = shapes.get(trip.shapeId());
            if (points != null && points.size() >= 2) {
                result.add(toPattern(trip, points));
            }
        }
        return result;
    }

    private static RoutePattern toPattern(Trip trip, List<ShapePoint> points) {
        int size = points.size();
        double[] lats = new double[size];
        double[] lons = new double[size];
        double[] cumulative = new double[size];
        for (int i = 0; i < size; i++) {
            lats[i] = points.get(i).shapePtLat();
            lons[i] = points.get(i).shapePtLon();
            if (i > 0) {
                cumulative[i] = cumulative[i - 1]
                        + GeoUtils.distanceMeters(lats[i - 1], lons[i - 1], lats[i], lons[i]);
            }
        }
        return new RoutePattern(trip.routeId(), trip.directionId(), trip.shapeId(), trip.tripHeadsign(),
                lats, lons, cumulative);
    }
}
