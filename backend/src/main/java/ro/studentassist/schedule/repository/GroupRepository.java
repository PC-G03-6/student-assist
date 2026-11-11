package ro.studentassist.schedule.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ro.studentassist.schedule.entity.GroupEntity;

import java.util.List;

/**
 * Repository JPA pentru grupele academice.
 */
public interface GroupRepository extends JpaRepository<GroupEntity, String> {

    List<GroupEntity> findAllByOrderByIdAsc();

    List<GroupEntity> findBySpecializationIdIgnoreCaseOrderByIdAsc(String specializationId);

    List<GroupEntity> findByYearOrderByIdAsc(Integer year);

    List<GroupEntity> findBySpecializationIdIgnoreCaseAndYearOrderByIdAsc(String specializationId, Integer year);

    List<GroupEntity> findBySpecializationIdInOrderByIdAsc(List<String> specializationIds);

    List<GroupEntity> findBySpecializationIdInAndYearOrderByIdAsc(List<String> specializationIds, Integer year);
}
