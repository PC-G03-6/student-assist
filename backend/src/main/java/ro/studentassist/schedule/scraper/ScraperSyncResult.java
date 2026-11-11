package ro.studentassist.schedule.scraper;

import java.util.List;

/**
 * Rezumatul unei execuții de scraping și salvare în baza de date SQLite.
 */
public record ScraperSyncResult(
        String configurationId,
        int specializationsSynced,
        int groupsSynced,
        int eventsSynced,
        int roomsSynced,
        List<String> failedPages
) {
}
