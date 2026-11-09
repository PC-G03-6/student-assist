package ro.studentassist.schedule.scraper;

import lombok.extern.slf4j.Slf4j;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;
import ro.studentassist.schedule.entity.GroupEntity;
import ro.studentassist.schedule.entity.RoomEntity;
import ro.studentassist.schedule.entity.ScheduleEventEntity;
import ro.studentassist.schedule.entity.SpecializationEntity;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parser HTML bazat pe Jsoup pentru paginile orarului oficial UBB FMI.
 * <p>
 * Înlocuiește parsarea fragilă prin string-slicing cu navigare DOM tolerantă la
 * modificări minore de formatare sau elemente lipsă.
 */
@Slf4j
@Component
public class UbbScheduleParser {

    private static final Pattern META_URL_PATTERN =
            Pattern.compile("url\\s*=\\s*['\"]?([^'\">\\s;]+)['\"]?", Pattern.CASE_INSENSITIVE);
    private static final Pattern CONFIG_ID_PATTERN =
            Pattern.compile("(20\\d{2}-[12])");
    private static final Pattern GRUPA_HEADER_PATTERN =
            Pattern.compile("^Grupa\\s+(.+)$", Pattern.CASE_INSENSITIVE);
    private static final Pattern DIGIT_PATTERN =
            Pattern.compile("(\\d+)");
    private static final Pattern HOURS_PATTERN =
            Pattern.compile("^(\\d{1,2})(?::\\d{2})?\\s*-\\s*(\\d{1,2})(?::\\d{2})?$");

    private static final Map<String, Integer> DAY_INDEX_MAP = Map.of(
            "luni", 1,
            "marti", 2,
            "miercuri", 3,
            "joi", 4,
            "vineri", 5,
            "sambata", 6,
            "duminica", 7
    );

    private static final Map<String, String> CANONICAL_DAY_MAP = Map.of(
            "luni", "Luni",
            "marti", "Marti",
            "miercuri", "Miercuri",
            "joi", "Joi",
            "vineri", "Vineri",
            "sambata", "Sambata",
            "duminica", "Duminica"
    );

    /**
     * Extrage configurația activă (ex: "2026-1") din pagina rădăcină /files/orar/.
     */
    public Optional<String> parseActiveConfiguration(Document rootDoc) {
        if (rootDoc == null) {
            return Optional.empty();
        }

        for (Element meta : rootDoc.select("meta[http-equiv]")) {
            if ("refresh".equalsIgnoreCase(meta.attr("http-equiv").trim())) {
                String content = meta.attr("content");
                Matcher urlMatcher = META_URL_PATTERN.matcher(content);
                if (urlMatcher.find()) {
                    String candidate = stripTrailingSlash(urlMatcher.group(1).trim());
                    Matcher configMatcher = CONFIG_ID_PATTERN.matcher(candidate);
                    if (configMatcher.find()) {
                        return Optional.of(configMatcher.group(1));
                    }
                    if (!candidate.isEmpty()) {
                        return Optional.of(candidate);
                    }
                }
            }
        }

        for (Element anchor : rootDoc.select("a[href]")) {
            Matcher matcher = CONFIG_ID_PATTERN.matcher(anchor.attr("href"));
            if (matcher.find()) {
                return Optional.of(matcher.group(1));
            }
        }

        return Optional.empty();
    }

    /**
     * Parsează tabelul de săli și adrese din pagina sali/legenda.html.
     */
    public List<RoomEntity> parseRoomsLegend(Document legendDoc) {
        if (legendDoc == null) {
            return Collections.emptyList();
        }

        Map<String, RoomEntity> rooms = new LinkedHashMap<>();
        for (Element row : legendDoc.select("table tr")) {
            Elements cells = row.select("td");
            if (cells.size() < 2) {
                continue;
            }

            Element roomCell = cells.get(0);
            Element detailsCell = cells.get(1);

            Element anchor = roomCell.selectFirst("a[href]");
            String roomName = cleanText(anchor != null ? anchor.text() : roomCell.text());
            if (roomName.isEmpty()) {
                continue;
            }

            String roomId = anchor != null
                    ? extractIdFromHref(anchor.attr("href"))
                    : sanitizeRoomId(roomName);
            if (roomId.isEmpty()) {
                roomId = sanitizeRoomId(roomName);
            }

            String details = cleanText(detailsCell.text());
            BuildingLocation location = deduceBuildingLocation(details);

            rooms.put(roomId, RoomEntity.builder()
                    .id(roomId)
                    .name(roomName)
                    .buildingName(location.buildingName())
                    .address(location.address())
                    .details(details)
                    .build());
        }

        return new ArrayList<>(rooms.values());
    }

    /**
     * Parsează indexul liniilor de studiu și anilor din pagina tabelar/index.html.
     */
    public List<SpecializationEntity> parseSpecializations(Document indexDoc, String configurationId) {
        if (indexDoc == null) {
            return Collections.emptyList();
        }

        Map<String, SpecializationEntity> specializations = new LinkedHashMap<>();
        for (Element table : indexDoc.select("table")) {
            String headerText = normalizeAscii(table.select("th").text()).toLowerCase(Locale.ROOT);
            String degree = headerText.contains("master") ? "master" : "licenta";

            for (Element row : table.select("tr")) {
                Elements cells = row.select("td");
                if (cells.size() < 2) {
                    continue;
                }

                String name = cleanText(cells.get(0).text());
                if (name.isEmpty()) {
                    continue;
                }

                String language = detectLanguage(name);
                String field = detectField(name);

                for (int i = 1; i < cells.size(); i++) {
                    Element anchor = cells.get(i).selectFirst("a[href]");
                    if (anchor == null) {
                        continue;
                    }

                    String specId = extractIdFromHref(anchor.attr("href"));
                    if (specId.isEmpty()) {
                        continue;
                    }

                    int year = extractYear(cleanText(anchor.text()), specId);
                    specializations.put(specId, SpecializationEntity.builder()
                            .id(specId)
                            .name(name)
                            .field(field)
                            .language(language)
                            .degree(degree)
                            .year(year)
                            .configurationId(configurationId)
                            .build());
                }
            }
        }

        return new ArrayList<>(specializations.values());
    }

    /**
     * Parsează grupele și toate evenimentele dintr-o pagină de linie de studiu (ex: I3.html, IE2.html).
     */
    public ParsedStudyLine parseStudyLineSchedule(
            Document studyLineDoc,
            SpecializationEntity specialization,
            Map<String, RoomEntity> roomsById
    ) {
        if (studyLineDoc == null || specialization == null) {
            return new ParsedStudyLine(Collections.emptyList(), Collections.emptyList());
        }

        Map<String, RoomEntity> safeRooms = roomsById != null ? roomsById : Collections.emptyMap();
        List<GroupEntity> groups = new ArrayList<>();
        Map<String, ScheduleEventEntity> deduplicatedEvents = new LinkedHashMap<>();

        for (Element h1 : studyLineDoc.select("h1")) {
            String headerText = cleanText(h1.text());
            Matcher grupaMatcher = GRUPA_HEADER_PATTERN.matcher(headerText);
            if (!grupaMatcher.matches()) {
                continue;
            }

            String groupId = cleanText(grupaMatcher.group(1));
            if (groupId.isEmpty()) {
                continue;
            }

            Element table = findFollowingTable(h1);
            if (table == null) {
                log.warn("Nu s-a găsit tabelul de orar după antetul '{}' pentru specializarea {}",
                        headerText, specialization.getId());
                continue;
            }

            Set<String> discoveredSubgroups = new TreeSet<>();
            for (Element row : table.select("tr")) {
                Elements cells = row.select("td");
                if (cells.isEmpty()) {
                    continue;
                }
                if (cells.size() < 8) {
                    log.debug("Rând incomplet ignorat în grupa {} ({} celule)", groupId, cells.size());
                    continue;
                }

                Optional<ScheduleEventEntity> parsedEvent = parseEventRow(
                        cells, groupId, specialization, safeRooms, discoveredSubgroups);
                parsedEvent.ifPresent(event -> deduplicatedEvents.put(event.getId(), event));
            }

            GroupEntity groupEntity = GroupEntity.builder()
                    .id(groupId)
                    .name("Grupa " + groupId)
                    .specializationId(specialization.getId())
                    .year(specialization.getYear())
                    .subgroupsCsv(String.join(",", discoveredSubgroups))
                    .configurationId(specialization.getConfigurationId())
                    .build();
            groups.add(groupEntity);
        }

        return new ParsedStudyLine(groups, new ArrayList<>(deduplicatedEvents.values()));
    }

    private Optional<ScheduleEventEntity> parseEventRow(
            Elements cells,
            String groupId,
            SpecializationEntity specialization,
            Map<String, RoomEntity> roomsById,
            Set<String> discoveredSubgroups
    ) {
        String rawDay = cleanText(cells.get(0).text());
        String normalizedDayKey = normalizeAscii(rawDay).toLowerCase(Locale.ROOT);
        Integer dayIndex = DAY_INDEX_MAP.get(normalizedDayKey);
        String canonicalDay = CANONICAL_DAY_MAP.get(normalizedDayKey);
        if (dayIndex == null || canonicalDay == null) {
            log.debug("Zi necunoscută ignorată '{}' în grupa {}", rawDay, groupId);
            return Optional.empty();
        }

        String rawHours = cleanText(cells.get(1).text());
        Matcher hoursMatcher = HOURS_PATTERN.matcher(rawHours);
        if (!hoursMatcher.matches()) {
            log.debug("Interval orar invalid ignorat '{}' în grupa {}", rawHours, groupId);
            return Optional.empty();
        }

        int startHour = Integer.parseInt(hoursMatcher.group(1));
        int endHour = Integer.parseInt(hoursMatcher.group(2));
        if (startHour < 0 || endHour > 24 || startHour >= endHour) {
            log.debug("Interval orar invalid {}-{} în grupa {}", startHour, endHour, groupId);
            return Optional.empty();
        }

        String startTime = String.format("%02d:00", startHour);
        String endTime = String.format("%02d:00", endHour);

        String rawFreq = normalizeAscii(cleanText(cells.get(2).text())).toLowerCase(Locale.ROOT);
        String frequency;
        String frequencyLabel;
        if (rawFreq.contains("1")) {
            frequency = "week1";
            frequencyLabel = "sapt. 1";
        } else if (rawFreq.contains("2")) {
            frequency = "week2";
            frequencyLabel = "sapt. 2";
        } else {
            frequency = "all";
            frequencyLabel = "săptămânal";
        }

        LinkCell roomCell = extractLinkCell(cells.get(3), "N/A");
        String roomId = roomCell.id();
        String roomName = roomCell.label();
        RoomEntity knownRoom = roomsById.get(roomId);
        String buildingName = knownRoom != null ? knownRoom.getBuildingName() : "Campus UBB";
        String roomAddress = knownRoom != null ? knownRoom.getAddress() : "Cluj-Napoca";

        String formation = cleanText(cells.get(4).text());
        if (formation.isEmpty()) {
            formation = groupId;
        }
        String subgroup = null;
        if (formation.contains("/") && !formation.equalsIgnoreCase(groupId)) {
            subgroup = formation;
            discoveredSubgroups.add(formation);
        }

        String eventType = cleanText(cells.get(5).text());
        if (eventType.isEmpty()) {
            eventType = "Curs";
        }

        LinkCell subjectCell = extractLinkCell(cells.get(6), "UNKNOWN_SUBJECT");
        LinkCell teacherCell = extractLinkCell(cells.get(7), "UNKNOWN_TEACHER");

        String configId = specialization.getConfigurationId() != null ? specialization.getConfigurationId() : "";
        String signature = String.join("|",
                configId,
                groupId,
                canonicalDay,
                startHour + "-" + endHour,
                frequency,
                roomId,
                formation,
                eventType,
                subjectCell.id(),
                teacherCell.id()
        );
        String eventId = sha256Hex(signature);

        return Optional.of(ScheduleEventEntity.builder()
                .id(eventId)
                .groupId(groupId)
                .subgroup(subgroup)
                .formation(formation)
                .dayName(canonicalDay)
                .dayIndex(dayIndex)
                .startHour(startHour)
                .endHour(endHour)
                .startTime(startTime)
                .endTime(endTime)
                .frequency(frequency)
                .frequencyLabel(frequencyLabel)
                .eventType(eventType)
                .subjectId(subjectCell.id())
                .subjectName(subjectCell.label())
                .teacherId(teacherCell.id())
                .teacherName(teacherCell.label())
                .roomId(roomId)
                .roomName(roomName)
                .buildingName(buildingName)
                .roomAddress(roomAddress)
                .configurationId(configId)
                .build());
    }

    private Element findFollowingTable(Element h1) {
        Element cursor = h1.nextElementSibling();
        while (cursor != null) {
            if ("table".equalsIgnoreCase(cursor.tagName())) {
                return cursor;
            }
            if ("h1".equalsIgnoreCase(cursor.tagName())) {
                return null;
            }
            cursor = cursor.nextElementSibling();
        }
        return null;
    }

    private LinkCell extractLinkCell(Element cell, String fallbackPrefix) {
        Element anchor = cell.selectFirst("a[href]");
        if (anchor != null) {
            String label = cleanText(anchor.text());
            String id = extractIdFromHref(anchor.attr("href"));
            if (id.isEmpty()) {
                id = label.isEmpty() ? fallbackPrefix : sanitizeRoomId(label);
            }
            return new LinkCell(id, label.isEmpty() ? id : label);
        }
        String text = cleanText(cell.text());
        if (text.isEmpty()) {
            return new LinkCell(fallbackPrefix, fallbackPrefix);
        }
        String fallbackId = sha256Hex(text).substring(0, 8);
        return new LinkCell(fallbackId, text);
    }

    private String extractIdFromHref(String href) {
        if (href == null || href.isBlank()) {
            return "";
        }
        String cleaned = href.trim();
        int slashIdx = cleaned.lastIndexOf('/');
        if (slashIdx >= 0) {
            cleaned = cleaned.substring(slashIdx + 1);
        }
        if (cleaned.toLowerCase(Locale.ROOT).endsWith(".html")) {
            cleaned = cleaned.substring(0, cleaned.length() - 5);
        }
        return cleaned.trim();
    }

    private String sanitizeRoomId(String roomName) {
        return roomName.replace('/', '-').replaceAll("\\s+", "");
    }

    private String detectLanguage(String specializationName) {
        String lower = normalizeAscii(specializationName).toLowerCase(Locale.ROOT);
        if (lower.contains("englez")) {
            return "engleza";
        }
        if (lower.contains("maghiar")) {
            return "maghiara";
        }
        if (lower.contains("german")) {
            return "germana";
        }
        return "romana";
    }

    private String detectField(String specializationName) {
        String cleaned = specializationName
                .replaceAll("(?i)\\s*-?\\s*linia de studiu.*$", "")
                .replaceAll("(?i)\\s*-?\\s*in limba.*$", "")
                .replaceAll("(?i)\\s*-?\\s*în limba.*$", "")
                .trim();
        return cleaned.isEmpty() ? specializationName : cleaned;
    }

    private int extractYear(String linkText, String specId) {
        Matcher linkMatcher = DIGIT_PATTERN.matcher(linkText);
        if (linkMatcher.find()) {
            return Integer.parseInt(linkMatcher.group(1));
        }
        Matcher idMatcher = Pattern.compile("(\\d+)$").matcher(specId);
        if (idMatcher.find()) {
            return Integer.parseInt(idMatcher.group(1));
        }
        return 1;
    }

    private BuildingLocation deduceBuildingLocation(String details) {
        String upper = normalizeAscii(details).toUpperCase(Locale.ROOT);
        if (upper.contains("CENTRALA") || upper.contains("KOGALNICEANU")) {
            return new BuildingLocation("Clădirea Centrală UBB", "Str. Mihail Kogălniceanu, nr. 1");
        }
        if (upper.contains("CANTEMIR")) {
            return new BuildingLocation("Clădirea Dimitrie Cantemir", "Str. Teodor Mihali, nr. 56");
        }
        if (upper.contains("FSEGA") || upper.contains("TEODOR MIHALI")) {
            return new BuildingLocation("Campus FSEGA", "Str. Teodor Mihali, nr. 58-60");
        }
        if (upper.contains("MATHEMATICA") || upper.contains("PLOIESTI")) {
            return new BuildingLocation("Clădirea Mathematica", "Str. Ploiești, nr. 23-25");
        }
        if (upper.contains("FIZICA")) {
            return new BuildingLocation("Facultatea de Fizică", "Str. Mihail Kogălniceanu, nr. 1");
        }
        if (upper.contains("DREPT") || upper.contains("MOSOIU")) {
            return new BuildingLocation("Facultatea de Drept", "Str. Traian Moșoiu, nr. 10-12");
        }
        if (upper.contains("NTT DATA") || upper.contains("CONSTANTA")) {
            return new BuildingLocation("Sediul NTT Data", "Str. Constanța, nr. 19-21");
        }
        if (upper.contains("AVRAM IANCU") || upper.contains("A. IANCU")) {
            return new BuildingLocation("Clădirea Avram Iancu", "Str. Avram Iancu");
        }
        if (upper.contains("DPPD") || upper.contains("PREGATIREA PERSONALULUI") || upper.contains("SINDICATELOR")) {
            return new BuildingLocation("DPPD UBB", "Str. Sindicatelor, nr. 7");
        }
        if (upper.contains("CHIMIE")) {
            return new BuildingLocation("Facultatea de Chimie", "Str. Arany János, nr. 11");
        }
        if (upper.contains("LITERE")) {
            return new BuildingLocation("Facultatea de Litere", "Str. Horea, nr. 31");
        }
        if (upper.contains("OBSERVATOR") || upper.contains("CIRESILOR")) {
            return new BuildingLocation("Observatorul Astronomic", "Str. Cireșilor");
        }
        if (upper.contains("SOCIOLOGIE") || upper.contains("SALIGNY")) {
            return new BuildingLocation("Facultatea de Sociologie", "Str. Anghel Saligny");
        }
        if (upper.contains("IOAN BOB")) {
            return new BuildingLocation("Școala Gimnazială Ioan Bob", "Str. Episcop Ioan Bob, nr. 10");
        }
        if (upper.contains("CODESPRING")) {
            return new BuildingLocation("Sediul firmei Codespring", "Cluj-Napoca");
        }
        if (upper.contains("MHP")) {
            return new BuildingLocation("Sediul MHP", "Cluj-Napoca");
        }
        if (upper.contains("VIRTUAL")) {
            return new BuildingLocation("Online / Virtual", "Online");
        }
        return new BuildingLocation("Campus UBB", "Cluj-Napoca");
    }

    private String cleanText(String text) {
        if (text == null) {
            return "";
        }
        return text.replace('\u00A0', ' ').replaceAll("\\s+", " ").trim();
    }

    private String normalizeAscii(String text) {
        if (text == null) {
            return "";
        }
        return Normalizer.normalize(text, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
    }

    private String stripTrailingSlash(String url) {
        String trimmed = url;
        while (trimmed.endsWith("/")) {
            trimmed = trimmed.substring(0, trimmed.length() - 1);
        }
        return trimmed;
    }

    private String sha256Hex(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 nu este disponibil în JVM", e);
        }
    }

    private record LinkCell(String id, String label) {
    }

    private record BuildingLocation(String buildingName, String address) {
    }
}
