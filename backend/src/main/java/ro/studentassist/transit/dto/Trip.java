package ro.studentassist.transit.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record Trip(
        @JsonAlias("direction_id") Integer directionId,
        @JsonAlias("route_id") Long routeId,
        @JsonAlias("trip_id") String tripId,
        @JsonAlias("trip_headsign") String tripHeadsign,
        @JsonAlias("block_id") Long blockId,
        @JsonAlias("shape_id") String shapeId,
        @JsonAlias("wheelchair_accessible") Integer wheelchairAccessible,
        @JsonAlias("bikes_allowed") Integer bikesAllowed) {
}
