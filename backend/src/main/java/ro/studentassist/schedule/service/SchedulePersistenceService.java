package ro.studentassist.schedule.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ro.studentassist.schedule.entity.GroupEntity;
import ro.studentassist.schedule.entity.RoomEntity;
import ro.studentassist.schedule.entity.ScheduleEventEntity;
import ro.studentassist.schedule.entity.SpecializationEntity;
import ro.studentassist.schedule.repository.GroupRepository;
import ro.studentassist.schedule.repository.RoomRepository;
import ro.studentassist.schedule.repository.ScheduleEventRepository;
import ro.studentassist.schedule.repository.SpecializationRepository;
import ro.studentassist.schedule.scraper.ParsedStudyLine;

import java.util.List;

/**
 * Serviciu responsabil strict de persistența tranzacțională în SQLite a datelor extrase de scraper.
 * Separat de apelurile HTTP pentru a nu ține blocaje pe SQLite în timpul descărcărilor.
 */
@Service
@RequiredArgsConstructor
public class SchedulePersistenceService {

    private final RoomRepository roomRepository;
    private final SpecializationRepository specializationRepository;
    private final GroupRepository groupRepository;
    private final ScheduleEventRepository scheduleEventRepository;

    @Transactional
    public void saveRooms(List<RoomEntity> rooms) {
        if (rooms != null && !rooms.isEmpty()) {
            roomRepository.saveAll(rooms);
        }
    }

    @Transactional
    public void saveSpecializations(List<SpecializationEntity> specializations) {
        if (specializations != null && !specializations.isEmpty()) {
            specializationRepository.saveAll(specializations);
        }
    }

    @Transactional
    public void saveStudyLineResult(ParsedStudyLine parsed) {
        if (parsed == null) {
            return;
        }

        List<GroupEntity> groups = parsed.groups();
        if (groups != null && !groups.isEmpty()) {
            groupRepository.saveAll(groups);
            List<String> groupIds = groups.stream().map(GroupEntity::getId).toList();
            scheduleEventRepository.deleteByGroupIdIn(groupIds);
        }

        List<ScheduleEventEntity> events = parsed.events();
        if (events != null && !events.isEmpty()) {
            scheduleEventRepository.saveAll(events);
        }
    }
}
