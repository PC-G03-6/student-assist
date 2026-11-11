package ro.studentassist.buildings.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ro.studentassist.buildings.model.Building;

public interface BuildingRepo extends JpaRepository<Building, Long> {

    boolean existsByCode(String code);
}
