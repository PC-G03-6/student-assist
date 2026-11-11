package ro.studentassist.transit;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import ro.studentassist.config.TimedCache;

@Service
public class TransitService {

    private final TimedCache<List<Stop>> stops;
    private final TimedCache<List<TransitRoute>> routes;
    private final TimedCache<List<Vehicle>> vehicles;
    private final TimedCache<List<Trip>> trips;
    private final TimedCache<Map<String, List<ShapePoint>>> shapes;

    public TransitService(TranzyClient client, TranzyProperties properties) {
        this.stops = new TimedCache<>(properties.staticCacheTtl(), client::fetchStops);
        this.routes = new TimedCache<>(properties.staticCacheTtl(), client::fetchRoutes);
        this.vehicles = new TimedCache<>(properties.vehiclesCacheTtl(), client::fetchVehicles);
        this.trips = new TimedCache<>(properties.staticCacheTtl(), client::fetchTrips);
        this.shapes = new TimedCache<>(properties.staticCacheTtl(), () -> groupShapes(client.fetchShapes()));
    }

    public List<Stop> getStops() {
        return stops.get();
    }

    public List<TransitRoute> getRoutes() {
        return routes.get();
    }

    public List<Vehicle> getVehicles() {
        return vehicles.get();
    }

    /** Returns only vehicles that have a route, coordinates and a position update from the last 90 seconds. */
    public List<Vehicle> getActiveVehicles() {
        Instant now = Instant.now();
        return getVehicles().stream()
                .filter(vehicle -> VehicleFilter.isActive(vehicle, now))
                .toList();
    }

    public List<Trip> getTrips() {
        return trips.get();
    }

    /** Returns every shape as a list of points ordered by sequence, keyed by shape id. */
    public Map<String, List<ShapePoint>> getShapes() {
        return shapes.get();
    }

    /** Groups raw shape points by shape id and sorts each group by point sequence. */
    private static Map<String, List<ShapePoint>> groupShapes(List<ShapePoint> points) {
        Map<String, List<ShapePoint>> grouped = new HashMap<>();
        for (ShapePoint point : points) {
            if (point.shapeId() == null
                    || point.shapePtLat() == null
                    || point.shapePtLon() == null
                    || point.shapePtSequence() == null) {
                continue;
            }
            grouped.computeIfAbsent(point.shapeId(), id -> new ArrayList<>()).add(point);
        }
        grouped.values().forEach(list -> list.sort(Comparator.comparing(ShapePoint::shapePtSequence)));
        return grouped;
    }
}
