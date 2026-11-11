package ro.studentassist.buildings;

import java.util.List;

public record RouteOption(
        Long routeId,
        String routeShortName,
        String routeLongName,
        Integer routeType,
        Integer directionId,
        String headsign,
        StopRef boardStop,
        StopRef alightStop,
        int walkToStopMinutes,
        int busArrivesInMinutes,
        int rideMinutes,
        int walkToBuildingMinutes,
        int totalMinutes,
        boolean liveEstimate,
        String vehicleLabel,
        List<Coordinate> path) {
}
