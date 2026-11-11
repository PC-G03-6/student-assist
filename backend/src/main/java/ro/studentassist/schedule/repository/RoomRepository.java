package ro.studentassist.schedule.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ro.studentassist.schedule.entity.RoomEntity;

/**
 * Repository JPA pentru sălile extrase din legenda UBB FMI.
 */
public interface RoomRepository extends JpaRepository<RoomEntity, String> {
}
