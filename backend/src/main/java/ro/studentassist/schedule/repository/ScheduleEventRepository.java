package ro.studentassist.schedule.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ro.studentassist.schedule.entity.ScheduleEventEntity;

import java.util.Collection;
import java.util.List;

/**
 * Repository JPA pentru evenimentele din orar.
 */
public interface ScheduleEventRepository extends JpaRepository<ScheduleEventEntity, String> {

    List<ScheduleEventEntity> findByGroupIdOrderByDayIndexAscStartHourAsc(String groupId);

    void deleteByGroupIdIn(Collection<String> groupIds);
}
