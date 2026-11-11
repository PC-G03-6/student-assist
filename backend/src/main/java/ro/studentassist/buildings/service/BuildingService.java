package ro.studentassist.buildings.service;

import java.util.List;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import ro.studentassist.buildings.exception.BuildingNotFoundException;
import ro.studentassist.buildings.model.Building;
import ro.studentassist.buildings.repository.BuildingRepo;

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
