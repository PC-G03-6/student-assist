package ro.studentassist.buildings;

public record StopRef(
        Long stopId,
        String stopName,
        double latitude,
        double longitude,
        int walkMeters) {
}
