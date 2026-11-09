package ro.studentassist.schedule.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Proprietăți configurabile pentru scraper-ul orarului UBB FMI.
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "studentassist.schedule")
public class ScheduleProperties {

    /**
     * URL-ul de bază unde sunt publicate orarele UBB FMI.
     */
    private String baseUrl = "https://www.cs.ubbcluj.ro/files/orar";

    /**
     * Configurația semestrială explicită (ex: "2026-1"). Dacă este goală, se detectează automat.
     */
    private String configurationId = "";

    /**
     * Timeout în milisecunde pentru cererile HTTP către site-ul UBB.
     */
    private int timeoutMs = 15000;

    /**
     * Întârziere între cererile consecutive pentru a evita supraîncărcarea serverului UBB.
     */
    private long requestDelayMs = 100;
}
