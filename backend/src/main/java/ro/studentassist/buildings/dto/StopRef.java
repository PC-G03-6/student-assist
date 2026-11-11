package ro.studentassist.buildings.dto;

public record StopRef(
        Long stopId,
        String stopName,
        double latitude,
        double longitude,
        int walkMeters) {
}
