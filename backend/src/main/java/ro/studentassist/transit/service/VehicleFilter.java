package ro.studentassist.transit;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeParseException;

final class VehicleFilter {

    private static final Duration MAX_AGE = Duration.ofSeconds(90);
    private static final Duration MAX_FUTURE_SKEW = Duration.ofSeconds(30);

    private VehicleFilter() {
    }

    /** Tells whether a vehicle has a route, valid coordinates and a recent position update. */
    static boolean isActive(Vehicle vehicle, Instant now) {
        if (vehicle.routeId() == null || vehicle.latitude() == null || vehicle.longitude() == null) {
            return false;
        }
        Instant timestamp = parseTimestamp(vehicle.timestamp());
        if (timestamp == null) {
            return false;
        }
        return !timestamp.isBefore(now.minus(MAX_AGE)) && !timestamp.isAfter(now.plus(MAX_FUTURE_SKEW));
    }

    /** Parses an ISO instant or a "yyyy-MM-dd HH:mm:ss" timestamp (read as UTC), returning null when invalid. */
    static Instant parseTimestamp(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        try {
            return Instant.parse(raw);
        } catch (DateTimeParseException ignored) {
            try {
                return LocalDateTime.parse(raw.replace(' ', 'T')).toInstant(ZoneOffset.UTC);
            } catch (DateTimeParseException alsoIgnored) {
                return null;
            }
        }
    }
}
