package ro.studentassist.schedule.scraper;

import ro.studentassist.schedule.entity.GroupEntity;
import ro.studentassist.schedule.entity.ScheduleEventEntity;

import java.util.List;

/**
 * Rezultatul parsării unei pagini HTML de linie de studiu (ex: I3.html).
 */
public record ParsedStudyLine(
        List<GroupEntity> groups,
        List<ScheduleEventEntity> events
) {
}
