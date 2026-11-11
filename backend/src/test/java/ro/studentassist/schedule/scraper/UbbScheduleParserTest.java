package ro.studentassist.schedule.scraper;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ro.studentassist.schedule.entity.GroupEntity;
import ro.studentassist.schedule.entity.RoomEntity;
import ro.studentassist.schedule.entity.ScheduleEventEntity;
import ro.studentassist.schedule.entity.SpecializationEntity;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Teste unitare pentru parser-ul Jsoup {@link UbbScheduleParser}.
 */
class UbbScheduleParserTest {

    private UbbScheduleParser parser;

    @BeforeEach
    void setUp() {
        parser = new UbbScheduleParser();
    }

    @Test
    void parseActiveConfiguration_extractsSemesterFromMetaRefreshAndFallbackLink() {
        String htmlWithMeta = """
                <!DOCTYPE html>
                <html>
                  <head>
                    <meta charset="UTF-8" />
                    <meta http-equiv="refresh" content="3; URL=2025-1" />
                  </head>
                  <body>
                    <p>Redirectare spre <a href="2025-1">aici</a>.</p>
                  </body>
                </html>
                """;

        Optional<String> config = parser.parseActiveConfiguration(Jsoup.parse(htmlWithMeta));
        assertThat(config).contains("2025-1");

        String htmlOnlyAnchor = """
                <html><body><p>Orar curent: <a href="2024-2/">2024-2</a></p></body></html>
                """;
        assertThat(parser.parseActiveConfiguration(Jsoup.parse(htmlOnlyAnchor))).contains("2024-2");
        assertThat(parser.parseActiveConfiguration(null)).isEmpty();
    }

    @Test
    void parseRoomsLegend_extractsRoomsBuildingsAndAddresses() {
        String legendHtml = """
                <html><body>
                <h1>Legenda salilor</h1>
                <table>
                  <tr><th>Sala</th><th>Localizarea</th></tr>
                  <tr>
                    <td><a href="7-I.html" >7/I</a></td>
                    <td>Sala D.V. Ionescu, Cladirea Centrala, etaj 1 (Str. M. Kogalniceanu)</td>
                  </tr>
                  <tr>
                    <td><a href="C335.html" >C335</a></td>
                    <td>Amfiteatru Campus FSEGA, str. Teodor Mihali nr. 56-60</td>
                  </tr>
                  <tr>
                    <td><a href="Saturn.html" >Saturn</a></td>
                    <td>Cladirea Mathematica, str. Ploiesti nr. 23-25</td>
                  </tr>
                </table>
                </body></html>
                """;

        List<RoomEntity> rooms = parser.parseRoomsLegend(Jsoup.parse(legendHtml));

        assertThat(rooms).hasSize(3);
        assertThat(rooms.get(0).getId()).isEqualTo("7-I");
        assertThat(rooms.get(0).getName()).isEqualTo("7/I");
        assertThat(rooms.get(0).getBuildingName()).isEqualTo("Clădirea Centrală UBB");
        assertThat(rooms.get(0).getAddress()).contains("Kogălniceanu");

        assertThat(rooms.get(1).getId()).isEqualTo("C335");
        assertThat(rooms.get(1).getBuildingName()).isEqualTo("Campus FSEGA");
        assertThat(rooms.get(1).getAddress()).contains("Teodor Mihali");

        assertThat(rooms.get(2).getBuildingName()).isEqualTo("Clădirea Mathematica");
    }

    @Test
    void parseRoomsLegend_maps2026BuildingsIncludingCantemirNttDataAvramIancuAndDppd() {
        String legendHtml = """
                <html><head><META HTTP-EQUIV="Content-Type" CONTENT="text/html; charset=UTF-8"></head><body>
                <table>
                  <tr><th>Sala</th><th>Localizarea</th></tr>
                  <tr><td><a href="DC402.html">DC402</a></td><td>Univ Dimitrie Cantemir, etaj 4 (str. Teodor Mihali nr. 56)</td></tr>
                  <tr><td><a href="NTTSocrate.html">NTTSocrate</a></td><td>Amfiteatrul Socrate - NTT Data, parter (str. Constanta, nr. 19-21)</td></tr>
                  <tr><td><a href="A303.html">A303</a></td><td>cladirea Avram Iancu, etaj 3 (Str. A. Iancu)</td></tr>
                  <tr><td><a href="DPPD-306.html">DPPD-306</a></td><td>Sala 306, Departamentul pentu Pregatirea Personalului Didactic</td></tr>
                  <tr><td><a href="Lit-Balzac.html">Lit-Balzac</a></td><td>Sala Balzac (Facultatea de Litere)</td></tr>
                  <tr><td><a href="Obs.html">Obs</a></td><td>Observatorul Astronomic (str. Ciresilor)</td></tr>
                  <tr><td><a href="Chimie134.html">Chimie134</a></td><td>Sala curs nr. 134 (Facultatea de Chimie)</td></tr>
                  <tr><td><a href="KuTi.html">KuTi</a></td><td>Amfiteatrul Kulcsar Tibor - Facultatea de Sociologie (str. Anghel Saligny)</td></tr>
                </table>
                </body></html>
                """;

        List<RoomEntity> rooms = parser.parseRoomsLegend(Jsoup.parse(legendHtml));

        assertThat(rooms).hasSize(8);
        assertThat(rooms.get(0).getBuildingName()).isEqualTo("Clădirea Dimitrie Cantemir");
        assertThat(rooms.get(0).getAddress()).isEqualTo("Str. Teodor Mihali, nr. 56");
        assertThat(rooms.get(1).getBuildingName()).isEqualTo("Sediul NTT Data");
        assertThat(rooms.get(2).getBuildingName()).isEqualTo("Clădirea Avram Iancu");
        assertThat(rooms.get(3).getBuildingName()).isEqualTo("DPPD UBB");
        assertThat(rooms.get(4).getBuildingName()).isEqualTo("Facultatea de Litere");
        assertThat(rooms.get(5).getBuildingName()).isEqualTo("Observatorul Astronomic");
        assertThat(rooms.get(6).getBuildingName()).isEqualTo("Facultatea de Chimie");
        assertThat(rooms.get(7).getBuildingName()).isEqualTo("Facultatea de Sociologie");
    }

    @Test
    void parseSpecializations_parsesLicentaAndMasterLinesAndSkipsNbsp() {
        String indexHtml = """
                <html><body>
                <table>
                  <tr><th>Studii Licenta</th><th>Anul</th><th>Anul</th><th>Anul</th></tr>
                  <tr>
                    <td>Informatica - linia de studiu romana</td>
                    <td><a href="I1.html" >Anul 1</a></td>
                    <td>&nbsp;</td>
                    <td><a href="I3.html" >Anul 3</a></td>
                  </tr>
                  <tr>
                    <td>Informatica - in limba engleza</td>
                    <td><a href="IE1.html" >Anul 1</a></td>
                    <td><a href="IE2.html" >Anul 2</a></td>
                    <td>&nbsp;</td>
                  </tr>
                  <tr>
                    <td>Matematica informatica - linia de studiu romana</td>
                    <td><a href="MI1.html" >Anul 1</a></td>
                    <td>&nbsp;</td>
                    <td>&nbsp;</td>
                  </tr>
                </table>
                <table>
                  <tr><th>Studii Master</th><th>Anul</th></tr>
                  <tr>
                    <td>Baze de date</td>
                    <td><a href="MaBD1.html" >Anul 1</a></td>
                  </tr>
                </table>
                </body></html>
                """;

        List<SpecializationEntity> specs = parser.parseSpecializations(Jsoup.parse(indexHtml), "2025-1");

        assertThat(specs).hasSize(6);
        assertThat(specs).extracting(SpecializationEntity::getId)
                .containsExactly("I1", "I3", "IE1", "IE2", "MI1", "MaBD1");

        SpecializationEntity ie2 = specs.stream().filter(s -> s.getId().equals("IE2")).findFirst().orElseThrow();
        assertThat(ie2.getLanguage()).isEqualTo("engleza");
        assertThat(ie2.getDegree()).isEqualTo("licenta");
        assertThat(ie2.getYear()).isEqualTo(2);
        assertThat(ie2.getField()).isEqualTo("Informatica");

        SpecializationEntity maBd1 = specs.stream().filter(s -> s.getId().equals("MaBD1")).findFirst().orElseThrow();
        assertThat(maBd1.getDegree()).isEqualTo("master");
        assertThat(maBd1.getLanguage()).isEqualTo("romana");
    }

    @Test
    void parseStudyLineSchedule_parsesInformaticaRomanaGroup231SubgroupsAndThreeHourInterval() {
        String i3Html = """
                <html><body>
                <h1>Orar Anul 3 Informatica - linia de studiu romana</h1>
                <h1>Grupa 231</h1>
                <table>
                  <tr><th>Ziua</th><th>Orele</th><th>Frecventa</th><th>Sala</th><th>Formatia</th><th>Tipul</th><th>Disciplina</th><th>Cadrul didactic</th></tr>
                  <tr>
                    <td>Luni</td><td>8-10</td><td>&nbsp;</td>
                    <td><a href="../sali/7-I.html">7/I</a></td><td>231</td><td>Seminar</td>
                    <td><a href="../disc/MLR5023.html">Limbaje formale si tehnici de compilare</a></td>
                    <td><a href="../cadre/poio1.html">Drd. POP Ioan Daniel</a></td>
                  </tr>
                  <tr>
                    <td>Luni</td><td>12-14</td><td>sapt. 2</td>
                    <td><a href="../sali/L320.html">L320</a></td><td>231/2</td><td>Laborator</td>
                    <td><a href="../disc/MLR5077.html">Programare paralela si distribuita</a></td>
                    <td><a href="../cadre/nivi.html">Conf. NICULESCU Virginia</a></td>
                  </tr>
                  <tr>
                    <td>Miercuri</td><td>12-15</td><td>sapt. 1</td>
                    <td><a href="../sali/7-I.html">7/I</a></td><td>I3</td><td>Curs</td>
                    <td><a href="../disc/VDP3506.html">Practica pedagogica</a></td>
                    <td><a href="../cadre/dima.html">Lect. MAIER Mariana</a></td>
                  </tr>
                </table>
                </body></html>
                """;

        SpecializationEntity i3 = SpecializationEntity.builder()
                .id("I3").name("Informatica romana").field("Informatica")
                .language("romana").degree("licenta").year(3).configurationId("2025-1")
                .build();
        Map<String, RoomEntity> rooms = Map.of(
                "7-I", new RoomEntity("7-I", "7/I", "Clădirea Centrală UBB", "Str. M. Kogălniceanu nr. 1", ""),
                "L320", new RoomEntity("L320", "L320", "Campus FSEGA", "Str. Teodor Mihali nr. 56-60", "")
        );

        ParsedStudyLine parsed = parser.parseStudyLineSchedule(Jsoup.parse(i3Html), i3, rooms);

        assertThat(parsed.groups()).hasSize(1);
        GroupEntity g231 = parsed.groups().get(0);
        assertThat(g231.getId()).isEqualTo("231");
        assertThat(g231.getSubgroupsList()).containsExactly("231/2");

        assertThat(parsed.events()).hasSize(3);

        ScheduleEventEntity lab = parsed.events().get(1);
        assertThat(lab.getFrequency()).isEqualTo("week2");
        assertThat(lab.getSubgroup()).isEqualTo("231/2");
        assertThat(lab.getBuildingName()).isEqualTo("Campus FSEGA");
        assertThat(lab.getId()).hasSize(64);

        ScheduleEventEntity threeHourCurs = parsed.events().get(2);
        assertThat(threeHourCurs.getStartHour()).isEqualTo(12);
        assertThat(threeHourCurs.getEndHour()).isEqualTo(15);
        assertThat(threeHourCurs.getStartTime()).isEqualTo("12:00");
        assertThat(threeHourCurs.getEndTime()).isEqualTo("15:00");
        assertThat(threeHourCurs.getFrequency()).isEqualTo("week1");
        assertThat(threeHourCurs.getSubgroup()).isNull();
    }

    @Test
    void parseStudyLineSchedule_parsesInformaticaEnglezaGroup921() {
        String ie2Html = """
                <html><body>
                <h1>Orar Anul 2 Informatica - in limba engleza</h1>
                <h1>Grupa 921</h1>
                <table>
                  <tr><th>Ziua</th><th>Orele</th><th>Frecventa</th><th>Sala</th><th>Formatia</th><th>Tipul</th><th>Disciplina</th><th>Cadrul didactic</th></tr>
                  <tr>
                    <td>Marti</td><td>10-12</td><td>&nbsp;</td>
                    <td><a href="../sali/C335.html">C335</a></td><td>IE2</td><td>Curs</td>
                    <td><a href="../disc/MLE5010.html">Database Management Systems</a></td>
                    <td><a href="../cadre/test1.html">Prof. TEST</a></td>
                  </tr>
                </table>
                </body></html>
                """;
        SpecializationEntity ie2 = SpecializationEntity.builder()
                .id("IE2").name("Informatica engleza").field("Informatica")
                .language("engleza").degree("licenta").year(2).configurationId("2025-1")
                .build();

        ParsedStudyLine parsed = parser.parseStudyLineSchedule(Jsoup.parse(ie2Html), ie2, Map.of());

        assertThat(parsed.groups()).hasSize(1);
        assertThat(parsed.groups().get(0).getId()).isEqualTo("921");
        assertThat(parsed.groups().get(0).getSpecializationId()).isEqualTo("IE2");
        assertThat(parsed.events()).hasSize(1);
        assertThat(parsed.events().get(0).getSubjectName()).isEqualTo("Database Management Systems");
    }

    @Test
    void parseStudyLineSchedule_parsesMasterGroupWithSlashInHeader() {
        String maBdHtml = """
                <html><body>
                <h1>Orar Anul 1 Baze de date</h1>
                <h1>Grupa 243/1</h1>
                <table>
                  <tr><th>Ziua</th><th>Orele</th><th>Frecventa</th><th>Sala</th><th>Formatia</th><th>Tipul</th><th>Disciplina</th><th>Cadrul didactic</th></tr>
                  <tr>
                    <td>Joi</td><td>16-18</td><td>&nbsp;</td>
                    <td><a href="../sali/C510.html">C510</a></td><td>243/1</td><td>Laborator</td>
                    <td><a href="../disc/MMR8001.html">Baze de date avansate</a></td>
                    <td><a href="../cadre/prof1.html">Conf. POPESCU</a></td>
                  </tr>
                </table>
                </body></html>
                """;
        SpecializationEntity maBd1 = SpecializationEntity.builder()
                .id("MaBD1").name("Baze de date").field("Baze de date")
                .language("romana").degree("master").year(1).configurationId("2025-1")
                .build();

        ParsedStudyLine parsed = parser.parseStudyLineSchedule(Jsoup.parse(maBdHtml), maBd1, Map.of());

        assertThat(parsed.groups()).hasSize(1);
        assertThat(parsed.groups().get(0).getId()).isEqualTo("243/1");
        assertThat(parsed.groups().get(0).getSubgroupsList()).isEmpty();
        assertThat(parsed.events()).hasSize(1);
        assertThat(parsed.events().get(0).getGroupId()).isEqualTo("243/1");
        assertThat(parsed.events().get(0).getSubgroup()).isNull();
    }

    @Test
    void parseStudyLineSchedule_preservesIso88592RomanianDiacritics() throws IOException {
        String isoHtml = """
                <html><head>
                <META HTTP-EQUIV="Content-Type" CONTENT="text/html; charset=ISO-8859-2">
                </head><body>
                <h1>Grupa 311</h1>
                <table>
                  <tr><th>Ziua</th><th>Orele</th><th>Frecventa</th><th>Sala</th><th>Formatia</th><th>Tipul</th><th>Disciplina</th><th>Cadrul didactic</th></tr>
                  <tr>
                    <td>Mar\u0163i</td><td>8-10</td><td>&nbsp;</td>
                    <td><a href="../sali/5-I.html">5/I</a></td><td>311</td><td>Curs</td>
                    <td><a href="../disc/MLR0001.html">\u015Etiin\u0163a calculatoarelor \u015Fi algoritmic\u0103</a></td>
                    <td><a href="../cadre/papa.html">Prof. P\u0103v\u0103loiu Ioan</a></td>
                  </tr>
                </table>
                </body></html>
                """;
        byte[] isoBytes = isoHtml.getBytes(Charset.forName("ISO-8859-2"));
        Document doc = Jsoup.parse(new ByteArrayInputStream(isoBytes), "ISO-8859-2", "https://cs.ubbcluj.ro/");

        SpecializationEntity mi1 = SpecializationEntity.builder()
                .id("MI1").name("Matematica informatica").field("Matematica informatica")
                .language("romana").degree("licenta").year(1).configurationId("2025-1")
                .build();

        ParsedStudyLine parsed = parser.parseStudyLineSchedule(doc, mi1, Map.of());

        assertThat(parsed.events()).hasSize(1);
        assertThat(parsed.events().get(0).getDayName()).isEqualTo("Marti");
        assertThat(parsed.events().get(0).getSubjectName()).isEqualTo("\u015Etiin\u0163a calculatoarelor \u015Fi algoritmic\u0103");
        assertThat(parsed.events().get(0).getTeacherName()).isEqualTo("Prof. P\u0103v\u0103loiu Ioan");
    }

    @Test
    void parseStudyLineSchedule_doesNotCrashOnMissingTablesMalformedRowsAndMissingAnchors() {
        String brokenHtml = """
                <html><body>
                <h1>Grupa 998</h1>
                <p>Orar indisponibil momentan</p>
                <h1>Grupa 999</h1>
                <table>
                  <tr><th>Ziua</th><th>Orele</th></tr>
                  <tr><td>Luni</td><td>8-10</td><td>celule incomplete</td></tr>
                  <tr><td>ZiInvalida</td><td>8-10</td><td>&nbsp;</td><td>S1</td><td>999</td><td>Curs</td><td>Mat</td><td>Prof</td></tr>
                  <tr><td>Luni</td><td>TBD</td><td>&nbsp;</td><td>S1</td><td>999</td><td>Curs</td><td>Mat</td><td>Prof</td></tr>
                  <tr><td>Luni</td><td>16-14</td><td>&nbsp;</td><td>S1</td><td>999</td><td>Curs</td><td>Mat</td><td>Prof</td></tr>
                  <tr>
                    <td>Vineri</td><td>10-12</td><td>&nbsp;</td>
                    <td>SalaFaraLink</td><td>999/1</td><td>Laborator</td>
                    <td>Materie fara link</td><td>Profesor fara link</td>
                  </tr>
                </table>
                </body></html>
                """;

        SpecializationEntity spec = SpecializationEntity.builder()
                .id("TEST1").name("Test").field("Test")
                .language("romana").degree("licenta").year(1).configurationId("2025-1")
                .build();

        ParsedStudyLine parsed = parser.parseStudyLineSchedule(Jsoup.parse(brokenHtml), spec, Map.of());

        assertThat(parsed.groups()).hasSize(1);
        assertThat(parsed.groups().get(0).getId()).isEqualTo("999");
        assertThat(parsed.events()).hasSize(1);

        ScheduleEventEntity validRow = parsed.events().get(0);
        assertThat(validRow.getRoomName()).isEqualTo("SalaFaraLink");
        assertThat(validRow.getSubjectName()).isEqualTo("Materie fara link");
        assertThat(validRow.getTeacherName()).isEqualTo("Profesor fara link");
        assertThat(validRow.getSubgroup()).isEqualTo("999/1");

        ParsedStudyLine nullResult = parser.parseStudyLineSchedule(null, spec, null);
        assertThat(nullResult.groups()).isEmpty();
        assertThat(nullResult.events()).isEmpty();
    }

    @Test
    void parseStudyLineSchedule_deduplicatesIdenticalRows() {
        String duplicateHtml = """
                <html><body>
                <h1>Grupa 231</h1>
                <table>
                  <tr>
                    <td>Luni</td><td>8-10</td><td>&nbsp;</td>
                    <td><a href="../sali/7-I.html">7/I</a></td><td>231</td><td>Curs</td>
                    <td><a href="../disc/MLR1.html">Algoritmi</a></td>
                    <td><a href="../cadre/t1.html">Prof. X</a></td>
                  </tr>
                  <tr>
                    <td>Luni</td><td>8-10</td><td>&nbsp;</td>
                    <td><a href="../sali/7-I.html">7/I</a></td><td>231</td><td>Curs</td>
                    <td><a href="../disc/MLR1.html">Algoritmi</a></td>
                    <td><a href="../cadre/t1.html">Prof. X</a></td>
                  </tr>
                </table>
                </body></html>
                """;
        SpecializationEntity spec = SpecializationEntity.builder()
                .id("I3").name("Info").field("Info")
                .language("romana").degree("licenta").year(3).configurationId("2025-1")
                .build();

        ParsedStudyLine parsed = parser.parseStudyLineSchedule(Jsoup.parse(duplicateHtml), spec, Map.of());
        assertThat(parsed.events()).hasSize(1);
    }

    @Test
    void parseRoomsLegendAndSpecializations_handleNullAndEmptyDocumentsGracefully() {
        assertThat(parser.parseRoomsLegend(null)).isEmpty();
        assertThat(parser.parseRoomsLegend(Jsoup.parse("<html><body></body></html>"))).isEmpty();
        assertThat(parser.parseSpecializations(null, "2025-1")).isEmpty();
        assertThat(parser.parseSpecializations(Jsoup.parse("<html><body><table></table></body></html>"), "2025-1"))
                .isEmpty();
    }
}
