package ro.studentassist.transit.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ShapePoint(
        @JsonAlias("shape_id") String shapeId,
        @JsonAlias("shape_pt_lat") Double shapePtLat,
        @JsonAlias("shape_pt_lon") Double shapePtLon,
        @JsonAlias("shape_pt_sequence") Integer shapePtSequence,
        @JsonAlias("shape_dist_traveled") Double shapeDistTraveled) {
}
