package ro.studentassist.transit;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Validated
@ConfigurationProperties(prefix = "tranzy")
public record TranzyProperties(
        @NotBlank String baseUrl,
        @NotBlank String apiKey,
        @NotBlank String apiKeyHeader,
        @NotBlank String agencyId,
        @NotNull Duration vehiclesCacheTtl,
        @NotNull Duration staticCacheTtl) {
}
