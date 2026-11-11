package ro.studentassist.buildings;

import java.util.List;

public record BuildingRouteResponse(
        BuildingResponse building,
        Coordinate origin,
        int directDistanceMeters,
        int walkOnlyMinutes,
        List<RouteOption> options) {
}
