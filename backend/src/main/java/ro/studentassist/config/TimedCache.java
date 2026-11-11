package ro.studentassist.config;

import java.time.Duration;
import java.time.Instant;
import java.util.function.Supplier;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TimedCache<T> {

    private final Duration ttl;
    private final Supplier<T> loader;
    private T value;
    private Instant loadedAt;

    public TimedCache(Duration ttl, Supplier<T> loader) {
        this.ttl = ttl;
        this.loader = loader;
    }

    /** Returns the cached value, reloading it when expired and serving stale data if the reload fails. */
    public synchronized T get() {
        Instant now = Instant.now();
        if (value != null && now.isBefore(loadedAt.plus(ttl))) {
            return value;
        }
        try {
            value = loader.get();
        } catch (RuntimeException ex) {
            if (value == null) {
                throw ex;
            }
            log.warn("Refresh failed, serving stale data", ex);
        }
        loadedAt = now;
        return value;
    }
}
