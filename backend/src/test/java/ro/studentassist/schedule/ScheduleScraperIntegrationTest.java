package ro.studentassist.schedule;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import ro.studentassist.schedule.client.UbbScheduleClient;
import ro.studentassist.schedule.config.ScheduleProperties;
import ro.studentassist.schedule.entity.GroupEntity;
import ro.studentassist.schedule.entity.ScheduleEventEntity;
import ro.studentassist.schedule.entity.SpecializationEntity;
import ro.studentassist.schedule.repository.GroupRepository;
import ro.studentassist.schedule.repository.RoomRepository;
import ro.studentassist.schedule.repository.ScheduleEventRepository;
import ro.studentassist.schedule.repository.SpecializationRepository;
import ro.studentassist.schedule.scraper.ScraperSyncResult;
import ro.studentassist.schedule.scraper.UbbScheduleParser;
import ro.studentassist.schedule.service.SchedulePersistenceService;
import ro.studentassist.schedule.service.ScheduleScraperService;
import ro.studentassist.schedule.service.ScheduleScraperServiceImpl;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Teste de integrare pentru scraper-ul Jsoup și salvarea în baza de date SQLite.
 * Demonstrează:
 * 1) Funcționarea pe 3 grupe din 3 specializări diferite (I3 -> 231, IE2 -> 921, MI1 -> 311).
 * 2) Listarea anilor, specializărilor și grupelor.
 * 3) Reziliența la pagini lipsă (HTTP 404 / 500) fără erori fatale.
 */
@SpringBootTest
class ScheduleScraperIntegrationTest {

    private static final Charset ISO_8859_2 = Charset.forName("ISO-8859-2");
    private static HttpServer mockUbbServer;
    private static int serverPort;
    private static volatile boolean simulateMissingIndex = false;
    private static volatile boolean expose2026Schedule = false;

    @Autowired
    private ScheduleScraperService scraperService;

    @Autowired
    private UbbScheduleClient scheduleClient;

    @Autowired
    private UbbScheduleParser scheduleParser;

    @Autowired
    private SchedulePersistenceService persistenceService;

    @Autowired
    private ScheduleProperties scheduleProperties;

    @Autowired
    private ScheduleEventRepository eventRepository;

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private SpecializationRepository specializationRepository;

    @Autowired
    private RoomRepository roomRepository;

    @DynamicPropertySource
    static void registerDynamicProperties(DynamicPropertyRegistry registry) throws IOException {
        if (mockUbbServer == null) {
            startMockUbbServer();
        }
        registry.add("studentassist.schedule.base-url", () -> "http://localhost:" + serverPort + "/orar");
    }

    @AfterAll
    static void stopMockServer() {
        if (mockUbbServer != null) {
            mockUbbServer.stop(0);
            mockUbbServer = null;
        }
    }

    @BeforeEach
    void setUp() {
        simulateMissingIndex = false;
        expose2026Schedule = false;
        scheduleProperties.setConfigurationId("");
        eventRepository.deleteAll();
        groupRepository.deleteAll();
        specializationRepository.deleteAll();
        roomRepository.deleteAll();
    }

    @Test
    void scrapeAndSaveAll_persistsThreeSpecializationsAndSurvivesMissing404And500Pages() {
        ScraperSyncResult syncResult = scraperService.scrapeAndSaveAll();

        assertThat(syncResult.configurationId()).isEqualTo("2025-1");
        assertThat(syncResult.specializationsSynced()).isEqualTo(3);
        assertThat(syncResult.groupsSynced()).isEqualTo(4); // 231, 232 (I3) + 921 (IE2) + 311 (MI1)
        assertThat(syncResult.eventsSynced()).isEqualTo(5);
        assertThat(syncResult.failedPages()).containsExactlyInAnyOrder(
                "2025-1/tabelar/MISSING404.html",
                "2025-1/tabelar/BROKEN500.html"
        );

        // Verificăm salvarea în SQLite a specializărilor, anilor, sălilor și celor 3 grupe din specializări diferite
        assertThat(specializationRepository.findAll()).hasSize(5);
        assertThat(roomRepository.findAll()).hasSize(2);

        GroupEntity g231 = groupRepository.findById("231").orElseThrow();
        assertThat(g231.getSpecializationId()).isEqualTo("I3");
        assertThat(g231.getYear()).isEqualTo(3);
        List<ScheduleEventEntity> events231 = eventRepository.findByGroupIdOrderByDayIndexAscStartHourAsc("231");
        assertThat(events231).hasSize(2);
        assertThat(events231.get(0).getBuildingName()).isEqualTo("Clădirea Centrală UBB");

        GroupEntity g921 = groupRepository.findById("921").orElseThrow();
        assertThat(g921.getSpecializationId()).isEqualTo("IE2");
        assertThat(g921.getYear()).isEqualTo(2);
        List<ScheduleEventEntity> events921 = eventRepository.findByGroupIdOrderByDayIndexAscStartHourAsc("921");
        assertThat(events921).hasSize(1);
        assertThat(events921.get(0).getSubjectName()).isEqualTo("Web Programming");

        GroupEntity g311 = groupRepository.findById("311").orElseThrow();
        assertThat(g311.getSpecializationId()).isEqualTo("MI1");
        assertThat(g311.getYear()).isEqualTo(1);
        List<ScheduleEventEntity> events311 = eventRepository.findByGroupIdOrderByDayIndexAscStartHourAsc("311");
        assertThat(events311).hasSize(1);
        assertThat(events311.get(0).getSubjectName()).isEqualTo("Algebr\u0103 \u015Fi geometrie");
    }

    @Test
    void listAvailableSpecializationsAndGroups_returnsParsedEntriesAndHandlesMissingPages() {
        List<SpecializationEntity> specs = scraperService.listAvailableSpecializations();
        assertThat(specs).extracting(SpecializationEntity::getId)
                .contains("I3", "IE2", "MI1");

        List<GroupEntity> i3Groups = scraperService.listGroupsForSpecialization("I3");
        assertThat(i3Groups).extracting(GroupEntity::getId)
                .containsExactly("231", "232");

        // Pagină lipsă (404) sau specializare inexistentă nu aruncă excepție
        assertThat(scraperService.listGroupsForSpecialization("MISSING404")).isEmpty();
        assertThat(scraperService.listGroupsForSpecialization("INEXISTENTA")).isEmpty();
    }

    @Test
    void scrapeAndSaveAll_doesNotCrashWhenIndexPageIsMissing() {
        simulateMissingIndex = true;

        ScraperSyncResult result = scraperService.scrapeAndSaveAll();

        assertThat(result.specializationsSynced()).isZero();
        assertThat(result.groupsSynced()).isZero();
        assertThat(result.failedPages()).contains("2025-1/tabelar/index.html");
    }

    @Test
    void scrapeAndSaveSpecializations_prefersCurrentCalendarConfig2026OverStaleMetaRefreshAndSupportsOverride() {
        expose2026Schedule = true;
        Clock november2026Clock = Clock.fixed(Instant.parse("2026-11-09T10:00:00Z"), ZoneId.of("Europe/Bucharest"));
        ScheduleScraperServiceImpl serviceIn2026 = new ScheduleScraperServiceImpl(
                scheduleClient, scheduleParser, persistenceService, roomRepository, scheduleProperties, november2026Clock);

        ScraperSyncResult result = serviceIn2026.scrapeAndSaveSpecializations(List.of("I3"));

        assertThat(result.configurationId()).isEqualTo("2026-1");
        assertThat(result.specializationsSynced()).isEqualTo(1);
        GroupEntity group231 = groupRepository.findById("231").orElseThrow();
        assertThat(group231.getConfigurationId()).isEqualTo("2026-1");
        List<ScheduleEventEntity> events = eventRepository.findByGroupIdOrderByDayIndexAscStartHourAsc("231");
        assertThat(events).hasSize(1);
        assertThat(events.get(0).getBuildingName()).isEqualTo("Clădirea Dimitrie Cantemir");

        // Verificăm și suprascrierea explicită prin configurationId
        scheduleProperties.setConfigurationId("2025-1");
        ScraperSyncResult overrideResult = serviceIn2026.scrapeAndSaveSpecializations(List.of("IE2"));
        assertThat(overrideResult.configurationId()).isEqualTo("2025-1");
        assertThat(groupRepository.findById("921").orElseThrow().getConfigurationId()).isEqualTo("2025-1");
    }

    private static void startMockUbbServer() throws IOException {
        mockUbbServer = HttpServer.create(new InetSocketAddress(0), 0);
        serverPort = mockUbbServer.getAddress().getPort();

        mockUbbServer.createContext("/orar/", exchange -> respondHtml(exchange, 200, """
                <!DOCTYPE html>
                <html><head><meta http-equiv="refresh" content="3; URL=2025-1" /></head><body></body></html>
                """));

        mockUbbServer.createContext("/orar/2025-1/sali/legenda.html", exchange -> respondHtml(exchange, 200, """
                <html><head><META HTTP-EQUIV="Content-Type" CONTENT="text/html; charset=ISO-8859-2"></head><body>
                <table>
                  <tr><th>Sala</th><th>Localizarea</th></tr>
                  <tr><td><a href="7-I.html">7/I</a></td><td>Cladirea Centrala, str. M. Kogalniceanu nr. 1</td></tr>
                  <tr><td><a href="C335.html">C335</a></td><td>Campus FSEGA, str. Teodor Mihali nr. 56-60</td></tr>
                </table>
                </body></html>
                """));

        mockUbbServer.createContext("/orar/2025-1/tabelar/index.html", exchange -> {
            if (simulateMissingIndex) {
                respondHtml(exchange, 404, "Not found");
                return;
            }
            respondHtml(exchange, 200, """
                    <html><head><META HTTP-EQUIV="Content-Type" CONTENT="text/html; charset=ISO-8859-2"></head><body>
                    <table>
                      <tr><th>Studii Licenta</th><th>Anul</th><th>Anul</th><th>Anul</th></tr>
                      <tr>
                        <td>Informatica - linia de studiu romana</td>
                        <td>&nbsp;</td><td>&nbsp;</td><td><a href="I3.html">Anul 3</a></td>
                      </tr>
                      <tr>
                        <td>Informatica - in limba engleza</td>
                        <td>&nbsp;</td><td><a href="IE2.html">Anul 2</a></td><td>&nbsp;</td>
                      </tr>
                      <tr>
                        <td>Matematica informatica - linia de studiu romana</td>
                        <td><a href="MI1.html">Anul 1</a></td><td>&nbsp;</td><td>&nbsp;</td>
                      </tr>
                      <tr>
                        <td>Specializare Inexistenta</td>
                        <td><a href="MISSING404.html">Anul 1</a></td><td>&nbsp;</td><td>&nbsp;</td>
                      </tr>
                      <tr>
                        <td>Specializare Cu Eroare Server</td>
                        <td><a href="BROKEN500.html">Anul 2</a></td><td>&nbsp;</td><td>&nbsp;</td>
                      </tr>
                    </table>
                    </body></html>
                    """);
        });

        mockUbbServer.createContext("/orar/2025-1/tabelar/I3.html", exchange -> respondHtml(exchange, 200, """
                <html><head><META HTTP-EQUIV="Content-Type" CONTENT="text/html; charset=ISO-8859-2"></head><body>
                <h1>Grupa 231</h1>
                <table>
                  <tr><th>Ziua</th><th>Orele</th><th>Frecventa</th><th>Sala</th><th>Formatia</th><th>Tipul</th><th>Disciplina</th><th>Cadrul didactic</th></tr>
                  <tr>
                    <td>Luni</td><td>8-10</td><td>&nbsp;</td>
                    <td><a href="../sali/7-I.html">7/I</a></td><td>231</td><td>Seminar</td>
                    <td><a href="../disc/MLR5023.html">Limbaje formale</a></td>
                    <td><a href="../cadre/poio1.html">Drd. POP Ioan</a></td>
                  </tr>
                  <tr>
                    <td>Marti</td><td>12-14</td><td>sapt. 2</td>
                    <td><a href="../sali/C335.html">C335</a></td><td>231/1</td><td>Laborator</td>
                    <td><a href="../disc/MLR5077.html">Programare paralela</a></td>
                    <td><a href="../cadre/nivi.html">Conf. NICULESCU Virginia</a></td>
                  </tr>
                </table>
                <h1>Grupa 232</h1>
                <table>
                  <tr><th>Ziua</th><th>Orele</th><th>Frecventa</th><th>Sala</th><th>Formatia</th><th>Tipul</th><th>Disciplina</th><th>Cadrul didactic</th></tr>
                  <tr>
                    <td>Miercuri</td><td>10-12</td><td>&nbsp;</td>
                    <td><a href="../sali/7-I.html">7/I</a></td><td>I3</td><td>Curs</td>
                    <td><a href="../disc/MLR5023.html">Limbaje formale</a></td>
                    <td><a href="../cadre/poio1.html">Drd. POP Ioan</a></td>
                  </tr>
                </table>
                </body></html>
                """));

        mockUbbServer.createContext("/orar/2025-1/tabelar/IE2.html", exchange -> respondHtml(exchange, 200, """
                <html><head><META HTTP-EQUIV="Content-Type" CONTENT="text/html; charset=ISO-8859-2"></head><body>
                <h1>Grupa 921</h1>
                <table>
                  <tr><th>Ziua</th><th>Orele</th><th>Frecventa</th><th>Sala</th><th>Formatia</th><th>Tipul</th><th>Disciplina</th><th>Cadrul didactic</th></tr>
                  <tr>
                    <td>Joi</td><td>14-16</td><td>&nbsp;</td>
                    <td><a href="../sali/C335.html">C335</a></td><td>921</td><td>Curs</td>
                    <td><a href="../disc/MLE5001.html">Web Programming</a></td>
                    <td><a href="../cadre/test.html">Lect. ENESCU</a></td>
                  </tr>
                </table>
                </body></html>
                """));

        mockUbbServer.createContext("/orar/2025-1/tabelar/MI1.html", exchange -> respondHtml(exchange, 200, """
                <html><head><META HTTP-EQUIV="Content-Type" CONTENT="text/html; charset=ISO-8859-2"></head><body>
                <h1>Grupa 311</h1>
                <table>
                  <tr><th>Ziua</th><th>Orele</th><th>Frecventa</th><th>Sala</th><th>Formatia</th><th>Tipul</th><th>Disciplina</th><th>Cadrul didactic</th></tr>
                  <tr>
                    <td>Vineri</td><td>10-12</td><td>&nbsp;</td>
                    <td><a href="../sali/7-I.html">7/I</a></td><td>311</td><td>Curs</td>
                    <td><a href="../disc/MMR0001.html">Algebr\u0103 \u015Fi geometrie</a></td>
                    <td><a href="../cadre/mat1.html">Prof. POPESCU</a></td>
                  </tr>
                </table>
                </body></html>
                """));

        mockUbbServer.createContext("/orar/2025-1/tabelar/MISSING404.html",
                exchange -> respondHtml(exchange, 404, "<h1>404 Not Found</h1>"));

        mockUbbServer.createContext("/orar/2025-1/tabelar/BROKEN500.html",
                exchange -> respondHtml(exchange, 500, "<h1>500 Internal Server Error</h1>"));

        mockUbbServer.createContext("/orar/2026-1/sali/legenda.html", exchange -> {
            if (!expose2026Schedule) {
                respondHtml(exchange, 404, "Not found");
                return;
            }
            respondHtml(exchange, 200, """
                    <html><head><META HTTP-EQUIV="Content-Type" CONTENT="text/html; charset=ISO-8859-2"></head><body>
                    <table>
                      <tr><th>Sala</th><th>Localizarea</th></tr>
                      <tr><td><a href="DC402.html">DC402</a></td><td>Univ Dimitrie Cantemir, etaj 4 (str. Teodor Mihali nr. 56)</td></tr>
                    </table>
                    </body></html>
                    """);
        });

        mockUbbServer.createContext("/orar/2026-1/tabelar/index.html", exchange -> {
            if (!expose2026Schedule) {
                respondHtml(exchange, 404, "Not found");
                return;
            }
            respondHtml(exchange, 200, """
                    <html><head><META HTTP-EQUIV="Content-Type" CONTENT="text/html; charset=ISO-8859-2"></head><body>
                    <table>
                      <tr><th>Studii Licenta</th><th>Anul</th><th>Anul</th><th>Anul</th></tr>
                      <tr>
                        <td>Informatica - linia de studiu romana</td>
                        <td>&nbsp;</td><td>&nbsp;</td><td><a href="I3.html">Anul 3</a></td>
                      </tr>
                    </table>
                    </body></html>
                    """);
        });

        mockUbbServer.createContext("/orar/2026-1/tabelar/I3.html", exchange -> {
            if (!expose2026Schedule) {
                respondHtml(exchange, 404, "Not found");
                return;
            }
            respondHtml(exchange, 200, """
                    <html><head><META HTTP-EQUIV="Content-Type" CONTENT="text/html; charset=ISO-8859-2"></head><body>
                    <h1>Grupa 231</h1>
                    <table>
                      <tr><th>Ziua</th><th>Orele</th><th>Frecventa</th><th>Sala</th><th>Formatia</th><th>Tipul</th><th>Disciplina</th><th>Cadrul didactic</th></tr>
                      <tr>
                        <td>Luni</td><td>8-10</td><td>&nbsp;</td>
                        <td><a href="../sali/DC402.html">DC402</a></td><td>231</td><td>Seminar</td>
                        <td><a href="../disc/MLR5023.html">Limbaje formale si tehnici de compilare</a></td>
                        <td><a href="../cadre/poio1.html">Asist. POP Ioan Daniel</a></td>
                      </tr>
                    </table>
                    </body></html>
                    """);
        });

        mockUbbServer.start();
    }

    private static void respondHtml(HttpExchange exchange, int status, String body) throws IOException {
        byte[] bytes = body.getBytes(ISO_8859_2);
        exchange.getResponseHeaders().add("Content-Type", "text/html; charset=ISO-8859-2");
        exchange.sendResponseHeaders(status, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }
}
