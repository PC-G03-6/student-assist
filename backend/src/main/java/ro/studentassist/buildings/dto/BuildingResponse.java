package ro.studentassist.buildings;

public record BuildingResponse(
        Long id,
        String code,
        String name,
        String address,
        double latitude,
        double longitude) {

    /** Converts a Building entity into its API representation. */
    static BuildingResponse from(Building building) {
        return new BuildingResponse(
                building.getId(),
                building.getCode(),
                building.getName(),
                building.getAddress(),
                building.getLatitude(),
                building.getLongitude());
    }
}
