package ro.studentassist.schedule.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Component;
import ro.studentassist.schedule.config.ScheduleProperties;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Optional;

/**
 * Implementare rezilientă bazată pe Jsoup pentru descărcarea paginilor HTML UBB FMI.
 * <p>
 * Gestionează explicit codarea ISO-8859-2 și tratează paginile lipsă (404/500/timeout)
 * prin avertismente în log fără a arunca excepții care ar opri sincronizarea.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JsoupUbbScheduleClient implements UbbScheduleClient {

    private static final String USER_AGENT = "StudentAssist/1.0 (UBB FMI Student Assistant)";
    private static final String DEFAULT_UBB_CHARSET = "ISO-8859-2";

    private final ScheduleProperties properties;

    @Override
    public Optional<Document> fetchRootPage() {
        return fetchAbsoluteUrl(stripTrailingSlash(properties.getBaseUrl()) + "/");
    }

    @Override
    public Optional<Document> fetchPage(String relativePath) {
        if (relativePath == null || relativePath.isBlank()) {
            return Optional.empty();
        }
        String base = stripTrailingSlash(properties.getBaseUrl());
        String cleanRelative = relativePath.startsWith("/") ? relativePath.substring(1) : relativePath;
        return fetchAbsoluteUrl(base + "/" + cleanRelative);
    }

    private Optional<Document> fetchAbsoluteUrl(String url) {
        try {
            Connection.Response response = Jsoup.connect(url)
                    .userAgent(USER_AGENT)
                    .timeout(properties.getTimeoutMs())
                    .ignoreHttpErrors(true)
                    .ignoreContentType(true)
                    .execute();

            int statusCode = response.statusCode();
            if (statusCode >= 400) {
                log.warn("Pagină lipsă sau indisponibilă (HTTP {}) la URL: {}", statusCode, url);
                return Optional.empty();
            }

            byte[] bodyBytes = response.bodyAsBytes();
            if (bodyBytes.length == 0) {
                log.warn("Răspuns HTTP gol primit de la URL: {}", url);
                return Optional.empty();
            }

            String charset = resolveCharset(response.charset(), bodyBytes);
            Document document = Jsoup.parse(new ByteArrayInputStream(bodyBytes), charset, url);
            return Optional.of(document);
        } catch (IOException | IllegalArgumentException e) {
            log.warn("Eroare la descărcarea paginii {}: {}", url, e.getMessage());
            return Optional.empty();
        }
    }

    private String resolveCharset(String httpCharset, byte[] bodyBytes) {
        if (httpCharset != null && !httpCharset.isBlank()) {
            return httpCharset;
        }
        String headSnippet = new String(
                bodyBytes,
                0,
                Math.min(bodyBytes.length, 1024),
                StandardCharsets.ISO_8859_1
        ).toLowerCase(Locale.ROOT);

        if (headSnippet.contains("charset=")) {
            return null; // Jsoup detectează automat charset-ul din tag-ul <meta>
        }
        return DEFAULT_UBB_CHARSET;
    }

    private String stripTrailingSlash(String url) {
        String trimmed = url != null ? url.trim() : "";
        while (trimmed.endsWith("/")) {
            trimmed = trimmed.substring(0, trimmed.length() - 1);
        }
        return trimmed;
    }
}
