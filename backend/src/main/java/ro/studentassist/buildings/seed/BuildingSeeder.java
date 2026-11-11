package ro.studentassist.buildings;

import java.util.List;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class BuildingSeeder implements ApplicationRunner {

    private final BuildingRepo buildingRepo;

    /** Inserts the default buildings that are not in the database yet. */
    @Override
    public void run(ApplicationArguments args) {
        defaultBuildings().stream()
                .filter(building -> !buildingRepo.existsByCode(building.getCode()))
                .forEach(buildingRepo::save);
    }

    private List<Building> defaultBuildings() {
        return List.of(
                building("CENTRAL", "Cladirea Centrala UBB", "Str. Mihail Kogalniceanu 1", 46.7675278, 23.5913333),
                building("AVRAM_IANCU", "Cladirea Avram Iancu", "Str. Avram Iancu", 46.7664167, 23.5896944),
                building("FSEGA", "Cladirea FSEGA", "Str. Teodor Mihali 58-60", 46.7732500, 23.6216111),
                building("DIMITRIE_CANTEMIR", "Facultatea Dimitrie Cantemir", "Str. Teodor Mihali 56", 46.7749167, 23.6211389),
                building("MATHEMATICA", "Cladirea Mathematica", "Str. Ploiesti 23-25", 46.7764444, 23.5950833),
                building("DREPT", "Facultatea de Drept", null, 46.7690000, 23.6004167));
    }

    private Building building(String code, String name, String address, double latitude, double longitude) {
        return Building.builder()
                .code(code)
                .name(name)
                .address(address)
                .latitude(latitude)
                .longitude(longitude)
                .build();
    }
}
