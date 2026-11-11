package ro.studentassist.transit.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record Vehicle(
        String id,
        String label,
        Double latitude,
        Double longitude,
        String timestamp,
        @JsonAlias("vehicle_type") Integer vehicleType,
        @JsonAlias("bike_accessible") String bikeAccessible,
        @JsonAlias("wheelchair_accessible") String wheelchairAccessible,
        Double speed,
        @JsonAlias("route_id") Long routeId,
        @JsonAlias("trip_id") String tripId) {
}
