package ro.studentassist.schedule.service;

import ro.studentassist.schedule.entity.GroupEntity;
import ro.studentassist.schedule.entity.SpecializationEntity;
import ro.studentassist.schedule.scraper.ScraperSyncResult;

import java.util.List;

/**
 * Serviciul responsabil exclusiv de extragerea (scraping) orarului UBB FMI
 * și salvarea acestuia în baza de date SQLite.
 */
public interface ScheduleScraperService {

    /**
     * Listează anii și specializările disponibile din pagina index UBB FMI.
     */
    List<SpecializationEntity> listAvailableSpecializations();

    /**
     * Listează grupele disponibile pentru o specializare dată de pe site-ul UBB FMI.
     */
    List<GroupEntity> listGroupsForSpecialization(String specializationId);

    /**
     * Extrage întregul orar UBB FMI (săli, specializări, grupe, ore) și îl salvează în SQLite.
     */
    ScraperSyncResult scrapeAndSaveAll();

    /**
     * Extrage și salvează în SQLite orarul doar pentru specializările indicate (ex: ["I3", "IE2", "MI1"]).
     */
    ScraperSyncResult scrapeAndSaveSpecializations(List<String> specializationIds);
}
