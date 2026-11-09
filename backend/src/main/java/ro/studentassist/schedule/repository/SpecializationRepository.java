package ro.studentassist.schedule.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ro.studentassist.schedule.entity.SpecializationEntity;

import java.util.List;

/**
 * Repository JPA pentru liniile de studiu / specializările UBB FMI.
 */
public interface SpecializationRepository extends JpaRepository<SpecializationEntity, String> {

    List<SpecializationEntity> findByDegreeIgnoreCase(String degree);

    List<SpecializationEntity> findByYear(Integer year);

    List<SpecializationEntity> findByDegreeIgnoreCaseAndYear(String degree, Integer year);
}
