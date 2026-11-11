package ro.studentassist.buildings;

import java.util.List;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BuildingService {

    private final BuildingRepo buildingRepo;

    /** Returns all buildings. */
    public List<Building> findAll() {
        return buildingRepo.findAll();
    }

    /** Returns the building with the given id or throws BuildingNotFoundException. */
    public Building getById(Long id) {
        return buildingRepo.findById(id).orElseThrow(() -> new BuildingNotFoundException(id));
    }
}
