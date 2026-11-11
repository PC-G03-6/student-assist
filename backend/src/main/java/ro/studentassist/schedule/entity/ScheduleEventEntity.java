package ro.studentassist.schedule.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Entitate JPA pentru un eveniment din orar (curs, seminar sau laborator).
 */
@Entity
@Table(
        name = "schedule_events",
        indexes = {
                @Index(name = "idx_schedule_events_group", columnList = "group_id")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScheduleEventEntity {

    @Id
    @Column(name = "id", nullable = false, length = 64)
    private String id;

    @Column(name = "group_id", nullable = false, length = 64)
    private String groupId;

    @Column(name = "subgroup", length = 64)
    private String subgroup;

    @Column(name = "formation", nullable = false, length = 64)
    private String formation;

    @Column(name = "day_name", nullable = false, length = 32)
    private String dayName;

    @Column(name = "day_index", nullable = false)
    private Integer dayIndex;

    @Column(name = "start_hour", nullable = false)
    private Integer startHour;

    @Column(name = "end_hour", nullable = false)
    private Integer endHour;

    @Column(name = "start_time", nullable = false, length = 16)
    private String startTime;

    @Column(name = "end_time", nullable = false, length = 16)
    private String endTime;

    @Column(name = "frequency", nullable = false, length = 16)
    private String frequency;

    @Column(name = "frequency_label", nullable = false, length = 32)
    private String frequencyLabel;

    @Column(name = "event_type", nullable = false, length = 32)
    private String eventType;

    @Column(name = "subject_id", nullable = false, length = 64)
    private String subjectId;

    @Column(name = "subject_name", nullable = false)
    private String subjectName;

    @Column(name = "teacher_id", nullable = false, length = 64)
    private String teacherId;

    @Column(name = "teacher_name", nullable = false)
    private String teacherName;

    @Column(name = "room_id", nullable = false, length = 64)
    private String roomId;

    @Column(name = "room_name", nullable = false)
    private String roomName;

    @Column(name = "building_name")
    private String buildingName;

    @Column(name = "room_address")
    private String roomAddress;

    @Column(name = "configuration_id", length = 32)
    private String configurationId;
}
