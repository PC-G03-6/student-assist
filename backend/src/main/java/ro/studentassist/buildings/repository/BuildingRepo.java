package ro.studentassist.buildings;

import org.springframework.data.jpa.repository.JpaRepository;

public interface BuildingRepo extends JpaRepository<Building, Long> {

    boolean existsByCode(String code);
}
