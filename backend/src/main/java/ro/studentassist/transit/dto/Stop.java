package ro.studentassist.transit.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record Stop(
        @JsonAlias("stop_id") Long stopId,
        @JsonAlias("stop_name") String stopName,
        @JsonAlias("stop_desc") String stopDesc,
        @JsonAlias("stop_lat") Double stopLat,
        @JsonAlias("stop_lon") Double stopLon,
        @JsonAlias("location_type") Integer locationType,
        @JsonAlias("stop_code") String stopCode) {
}
