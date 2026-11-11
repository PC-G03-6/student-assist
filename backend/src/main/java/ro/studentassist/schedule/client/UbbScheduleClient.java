package ro.studentassist.schedule.client;

import org.jsoup.nodes.Document;

import java.util.Optional;

/**
 * Client HTTP pentru preluarea paginilor HTML ale orarului UBB FMI.
 * Orice pagină lipsă (404) sau eroare de rețea întoarce Optional.empty() fără a opri procesarea.
 */
public interface UbbScheduleClient {

    /**
     * Descarcă pagina rădăcină și extrage identificatorul semestrului activ (ex: "2025-1").
     */
    Optional<Document> fetchRootPage();

    /**
     * Descarcă o pagină relativă la URL-ul de bază (ex: "2025-1/tabelar/index.html").
     */
    Optional<Document> fetchPage(String relativePath);
}
