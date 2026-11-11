package ro.studentassist.transit;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record TransitRoute(
        @JsonAlias("route_id") Long routeId,
        @JsonAlias("agency_id") Long agencyId,
        @JsonAlias("route_short_name") String routeShortName,
        @JsonAlias("route_long_name") String routeLongName,
        @JsonAlias("route_color") String routeColor,
        @JsonAlias("route_type") Integer routeType,
        @JsonAlias("route_desc") String routeDesc) {
}
