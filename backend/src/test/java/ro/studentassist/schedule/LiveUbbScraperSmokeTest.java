package ro.studentassist.schedule;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ro.studentassist.schedule.client.UbbScheduleClient;
import ro.studentassist.schedule.entity.GroupEntity;
import ro.studentassist.schedule.repository.GroupRepository;
import ro.studentassist.schedule.repository.ScheduleEventRepository;
import ro.studentassist.schedule.scraper.ScraperSyncResult;
import ro.studentassist.schedule.service.ScheduleScraperService;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test smoke live contra serverului real https://www.cs.ubbcluj.ro/files/orar/.
 * Rulează doar când este activat explicit cu: {@code ./gradlew test -DliveTests=true}.
 */
@SpringBootTest
@EnabledIfSystemProperty(named = "liveTests", matches = "true")
class LiveUbbScraperSmokeTest {

    @Autowired
    private ScheduleScraperService scraperService;

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private ScheduleEventRepository eventRepository;

    @Autowired
    private UbbScheduleClient scheduleClient;

    @Test
    void scrapesThreeDifferentSpecializationsIntoDbAndSurvivesMissingLivePage() {
        ScraperSyncResult result = scraperService.scrapeAndSaveSpecializations(List.of("I3", "IE2", "MIDS1"));

        assertThat(result.configurationId()).isEqualTo("2026-1");
        assertThat(result.specializationsSynced()).isEqualTo(3);
        assertThat(result.groupsSynced()).isGreaterThanOrEqualTo(3);
        assertThat(result.eventsSynced()).isGreaterThan(20);
        assertThat(result.roomsSynced()).isGreaterThan(10);

        GroupEntity group231 = groupRepository.findById("231").orElseThrow();
        GroupEntity group921 = groupRepository.findById("921").orElseThrow();
        GroupEntity group1511 = groupRepository.findById("1511").orElseThrow();

        assertThat(group231.getSpecializationId()).isEqualTo("I3");
        assertThat(group231.getConfigurationId()).isEqualTo("2026-1");
        assertThat(group921.getSpecializationId()).isEqualTo("IE2");
        assertThat(group921.getConfigurationId()).isEqualTo("2026-1");
        assertThat(group1511.getSpecializationId()).isEqualTo("MIDS1");
        assertThat(group1511.getConfigurationId()).isEqualTo("2026-1");

        assertThat(eventRepository.findByGroupIdOrderByDayIndexAscStartHourAsc("231")).isNotEmpty();
        assertThat(eventRepository.findByGroupIdOrderByDayIndexAscStartHourAsc("921")).isNotEmpty();
        assertThat(eventRepository.findByGroupIdOrderByDayIndexAscStartHourAsc("1511")).isNotEmpty();

        // Verificăm că o pagină 404 pe serverul real nu produce excepții
        assertThat(scheduleClient.fetchPage(result.configurationId() + "/tabelar/PAGINA_INEXISTENTA_404.html"))
                .isEmpty();
    }
}
