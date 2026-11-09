package ro.studentassist.schedule.service;

import lombok.extern.slf4j.Slf4j;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ro.studentassist.schedule.client.UbbScheduleClient;
import ro.studentassist.schedule.config.ScheduleProperties;
import ro.studentassist.schedule.entity.GroupEntity;
import ro.studentassist.schedule.entity.RoomEntity;
import ro.studentassist.schedule.entity.SpecializationEntity;
import ro.studentassist.schedule.repository.RoomRepository;
import ro.studentassist.schedule.scraper.ParsedStudyLine;
import ro.studentassist.schedule.scraper.ScraperSyncResult;
import ro.studentassist.schedule.scraper.UbbScheduleParser;

import java.time.Clock;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Implementarea serviciului de scraping Jsoup pentru orarul UBB FMI și salvare în SQLite.
 */
@Slf4j
@Service
public class ScheduleScraperServiceImpl implements ScheduleScraperService {

    private final UbbScheduleClient scheduleClient;
    private final UbbScheduleParser scheduleParser;
    private final SchedulePersistenceService persistenceService;
    private final RoomRepository roomRepository;
    private final ScheduleProperties properties;
    private final Clock clock;

    @Autowired
    public ScheduleScraperServiceImpl(
            UbbScheduleClient scheduleClient,
            UbbScheduleParser scheduleParser,
            SchedulePersistenceService persistenceService,
            RoomRepository roomRepository,
            ScheduleProperties properties
    ) {
        this(scheduleClient, scheduleParser, persistenceService, roomRepository, properties, Clock.systemDefaultZone());
    }

    public ScheduleScraperServiceImpl(
            UbbScheduleClient scheduleClient,
            UbbScheduleParser scheduleParser,
            SchedulePersistenceService persistenceService,
            RoomRepository roomRepository,
            ScheduleProperties properties,
            Clock clock
    ) {
        this.scheduleClient = scheduleClient;
        this.scheduleParser = scheduleParser;
        this.persistenceService = persistenceService;
        this.roomRepository = roomRepository;
        this.properties = properties;
        this.clock = clock;
    }

    @Override
    public List<SpecializationEntity> listAvailableSpecializations() {
        List<String> failedPages = new ArrayList<>();
        ResolvedIndex resolved = resolveActiveIndex(failedPages);
        if (resolved.indexDoc().isEmpty()) {
            log.warn("Nu s-au putut lista specializările: pagina '{}/tabelar/index.html' lipsește.",
                    resolved.configurationId());
            return Collections.emptyList();
        }
        return scheduleParser.parseSpecializations(resolved.indexDoc().get(), resolved.configurationId());
    }

    @Override
    public List<GroupEntity> listGroupsForSpecialization(String specializationId) {
        if (specializationId == null || specializationId.isBlank()) {
            return Collections.emptyList();
        }
        List<SpecializationEntity> specializations = listAvailableSpecializations();
        Optional<SpecializationEntity> matched = specializations.stream()
                .filter(s -> s.getId().equalsIgnoreCase(specializationId.trim()))
                .findFirst();

        if (matched.isEmpty()) {
            log.warn("Specializarea '{}' nu a fost găsită în indexul UBB.", specializationId);
            return Collections.emptyList();
        }

        SpecializationEntity spec = matched.get();
        String specPath = spec.getConfigurationId() + "/tabelar/" + spec.getId() + ".html";
        Optional<Document> specDoc = scheduleClient.fetchPage(specPath);
        if (specDoc.isEmpty()) {
            log.warn("Pagina specializării '{}' lipsește.", specPath);
            return Collections.emptyList();
        }
        return scheduleParser.parseStudyLineSchedule(specDoc.get(), spec, Collections.emptyMap()).groups();
    }

    @Override
    public ScraperSyncResult scrapeAndSaveAll() {
        return scrapeAndSaveInternal(null);
    }

    @Override
    public ScraperSyncResult scrapeAndSaveSpecializations(List<String> specializationIds) {
        return scrapeAndSaveInternal(specializationIds);
    }

    private ScraperSyncResult scrapeAndSaveInternal(List<String> targetSpecializations) {
        List<String> failedPages = new ArrayList<>();

        ResolvedIndex resolved = resolveActiveIndex(failedPages);
        String configurationId = resolved.configurationId();
        log.info("Începe scraping-ul orarului UBB FMI pentru configurația '{}'", configurationId);

        Map<String, RoomEntity> roomsById = syncRoomsLegend(configurationId, failedPages);

        String indexRelativePath = configurationId + "/tabelar/index.html";
        Optional<Document> indexDocOpt = resolved.indexDoc();
        if (indexDocOpt.isEmpty()) {
            log.warn("Scraping oprit: pagina index '{}' lipsește sau nu poate fi accesată.", indexRelativePath);
            return new ScraperSyncResult(configurationId, 0, 0, 0, roomsById.size(), failedPages);
        }

        List<SpecializationEntity> allSpecs = scheduleParser.parseSpecializations(indexDocOpt.get(), configurationId);
        List<SpecializationEntity> specsToSync = filterSpecializations(allSpecs, targetSpecializations);

        if (specsToSync.isEmpty()) {
            log.warn("Nu s-au găsit specializări de procesat în '{}'.", indexRelativePath);
            return new ScraperSyncResult(configurationId, 0, 0, 0, roomsById.size(), failedPages);
        }

        persistenceService.saveSpecializations(specsToSync);

        int specsSynced = 0;
        int groupsSynced = 0;
        int eventsSynced = 0;

        for (SpecializationEntity spec : specsToSync) {
            applyPoliteDelay();

            String specPath = configurationId + "/tabelar/" + spec.getId() + ".html";
            Optional<Document> specDocOpt = scheduleClient.fetchPage(specPath);
            if (specDocOpt.isEmpty()) {
                failedPages.add(specPath);
                log.warn("Pagină specializare lipsă sau inaccesibilă: '{}' — se continuă cu restul.", specPath);
                continue;
            }

            ParsedStudyLine parsed = scheduleParser.parseStudyLineSchedule(specDocOpt.get(), spec, roomsById);
            persistenceService.saveStudyLineResult(parsed);

            specsSynced++;
            groupsSynced += parsed.groups().size();
            eventsSynced += parsed.events().size();
        }

        log.info("Scraping finalizat ({}): {} specializări, {} grupe, {} evenimente salvate în DB ({} pagini lipsă).",
                configurationId, specsSynced, groupsSynced, eventsSynced, failedPages.size());

        return new ScraperSyncResult(
                configurationId,
                specsSynced,
                groupsSynced,
                eventsSynced,
                roomsById.size(),
                failedPages
        );
    }

    private ResolvedIndex resolveActiveIndex(List<String> failedPages) {
        String configuredId = properties.getConfigurationId();
        if (configuredId != null && !configuredId.isBlank()) {
            String trimmedConfig = configuredId.trim();
            String indexPath = trimmedConfig + "/tabelar/index.html";
            Optional<Document> indexDoc = scheduleClient.fetchPage(indexPath);
            if (indexDoc.isEmpty()) {
                failedPages.add(indexPath);
            }
            return new ResolvedIndex(trimmedConfig, indexDoc);
        }

        String calendarConfig = computeCurrentAcademicConfiguration();
        Optional<Document> rootDoc = scheduleClient.fetchRootPage();
        Optional<String> metaConfigOpt = rootDoc.flatMap(scheduleParser::parseActiveConfiguration);

        if (metaConfigOpt.isPresent()) {
            String metaConfig = metaConfigOpt.get();
            if (metaConfig.matches("20\\d{2}-[12]") && calendarConfig.compareTo(metaConfig) > 0) {
                Optional<Document> calendarIndexDoc = fetchValidIndex(calendarConfig);
                if (calendarIndexDoc.isPresent()) {
                    log.info("Detectată configurația semestrială actuală '{}' (rădăcina UBB indică încă '{}').",
                            calendarConfig, metaConfig);
                    return new ResolvedIndex(calendarConfig, calendarIndexDoc);
                }
            }

            String metaIndexPath = metaConfig + "/tabelar/index.html";
            Optional<Document> metaIndexDoc = scheduleClient.fetchPage(metaIndexPath);
            if (metaIndexDoc.isEmpty()) {
                failedPages.add(metaIndexPath);
            }
            return new ResolvedIndex(metaConfig, metaIndexDoc);
        }

        if (rootDoc.isEmpty()) {
            failedPages.add("/");
        }
        String calendarIndexPath = calendarConfig + "/tabelar/index.html";
        Optional<Document> calendarIndexDoc = fetchValidIndex(calendarConfig);
        if (calendarIndexDoc.isEmpty()) {
            failedPages.add(calendarIndexPath);
        }
        log.warn("Nu s-a putut citi configurația din rădăcina UBB; se folosește '{}'.", calendarConfig);
        return new ResolvedIndex(calendarConfig, calendarIndexDoc);
    }

    private Optional<Document> fetchValidIndex(String configId) {
        Optional<Document> doc = scheduleClient.fetchPage(configId + "/tabelar/index.html");
        if (doc.isPresent() && !scheduleParser.parseSpecializations(doc.get(), configId).isEmpty()) {
            return doc;
        }
        return Optional.empty();
    }

    private String computeCurrentAcademicConfiguration() {
        LocalDate now = LocalDate.now(clock);
        int month = now.getMonthValue();
        int academicStartYear = month >= 9 ? now.getYear() : now.getYear() - 1;
        int semester = (month >= 2 && month <= 8) ? 2 : 1;
        return academicStartYear + "-" + semester;
    }

    private record ResolvedIndex(String configurationId, Optional<Document> indexDoc) {
    }

    private Map<String, RoomEntity> syncRoomsLegend(String configurationId, List<String> failedPages) {
        Map<String, RoomEntity> roomsById = new HashMap<>();
        roomRepository.findAll().forEach(room -> roomsById.put(room.getId(), room));

        String legendPath = configurationId + "/sali/legenda.html";
        Optional<Document> legendDoc = scheduleClient.fetchPage(legendPath);
        if (legendDoc.isEmpty()) {
            failedPages.add(legendPath);
            log.warn("Legenda sălilor '{}' indisponibilă; se păstrează datele existente.", legendPath);
            return roomsById;
        }

        List<RoomEntity> parsedRooms = scheduleParser.parseRoomsLegend(legendDoc.get());
        persistenceService.saveRooms(parsedRooms);
        parsedRooms.forEach(room -> roomsById.put(room.getId(), room));
        return roomsById;
    }

    private List<SpecializationEntity> filterSpecializations(
            List<SpecializationEntity> allSpecs,
            List<String> targetSpecializations
    ) {
        if (targetSpecializations == null || targetSpecializations.isEmpty()) {
            return allSpecs;
        }
        Set<String> targets = targetSpecializations.stream()
                .filter(s -> s != null && !s.isBlank())
                .map(s -> s.trim().toLowerCase(Locale.ROOT))
                .collect(Collectors.toSet());
        if (targets.isEmpty()) {
            return allSpecs;
        }
        return allSpecs.stream()
                .filter(spec -> targets.contains(spec.getId().toLowerCase(Locale.ROOT)))
                .toList();
    }

    private void applyPoliteDelay() {
        long delayMs = properties.getRequestDelayMs();
        if (delayMs > 0) {
            try {
                Thread.sleep(delayMs);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
