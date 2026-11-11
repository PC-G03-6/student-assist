package ro.studentassist.schedule.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configurație internă a modulului {@code schedule} pentru activarea proprietăților scraper-ului.
 */
@Configuration
@EnableConfigurationProperties(ScheduleProperties.class)
public class ScheduleScraperConfig {
}
