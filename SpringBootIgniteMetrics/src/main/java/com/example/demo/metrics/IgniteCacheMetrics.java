package com.example.demo.metrics;

import io.micrometer.core.instrument.*;
import io.micrometer.core.instrument.binder.cache.CacheMeterBinder;
import lombok.extern.slf4j.Slf4j;
import org.apache.ignite.Ignite;
import org.apache.ignite.cache.CacheMetrics;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static java.util.concurrent.TimeUnit.MICROSECONDS;

/**
 * This binder will add appropriate metrics to the provided registry.
 */
@Slf4j
public class IgniteCacheMetrics extends CacheMeterBinder {

    private final AtomicReference<CacheMetrics> metrics = new AtomicReference<>(null);
    private final AtomicBoolean shutdown = new AtomicBoolean(false);
    private final String cacheName;
    private final Ignite ignite;
    private final long metricsRefreshFrequencyMillis;

    public static IgniteCacheMetrics monitor(MeterRegistry registry, Ignite ignite, String cache, long metricsRefreshFrequencyMillis, String... tags) {
        final IgniteCacheMetrics cacheMetrics = new IgniteCacheMetrics(ignite, cache, metricsRefreshFrequencyMillis, Tags.of(tags));
        cacheMetrics.bindTo(registry);
        log.info("Bound cache metrics monitor {} with registry {}", cache, registry);
        return cacheMetrics;
    }

    public IgniteCacheMetrics(Ignite ignite, String cacheName, long metricsRefreshFrequencyMillis, Iterable<Tag> tags) {
        super(ignite.cache(cacheName), cacheName, tags);
        this.ignite = ignite;
        this.cacheName = cacheName;
        this.metricsRefreshFrequencyMillis = metricsRefreshFrequencyMillis;
        refreshTask().run();
    }

    public void refresh(CacheMetrics newMetrics) {
        log.debug("Refreshing cache metrics {}", newMetrics);
        metrics.set(newMetrics);
    }

    Runnable refreshTask() {
        return () -> {
            if (shutdown.get()) {
                log.info("Cache metrics monitoring is shutdown.");
                return;
            }
            refresh(ignite.cache(cacheName).metrics());
            ignite.scheduler().runLocal(refreshTask(), metricsRefreshFrequencyMillis, TimeUnit.MILLISECONDS);
        };
    }

    public void unregister(MeterRegistry registry) {
        shutdown.set(true);
        final List<Meter> meters = registry.getMeters();
        for (final Meter meter : meters) {
            if (cacheName.equals(meter.getId().getTag("cache"))) {
                registry.remove(meter);
            }
        }
    }

    @Override
    protected Long size() {
        return metrics().getCacheSize();
    }

    private CacheMetrics metrics() {
        return metrics.get();
    }

    @Override
    protected long hitCount() {
        return metrics().getCacheHits();
    }

    @Override
    protected Long missCount() {
        return metrics().getCacheMisses();
    }

    @Override
    protected Long evictionCount() {
        return metrics().getCacheEvictions();
    }

    @Override
    protected long putCount() {
        return metrics().getCachePuts();
    }

    @Override
    protected void bindImplementationSpecificMetrics(MeterRegistry registry) {
        Gauge.builder("cache.gets.averagetime", cacheName, cache -> MICROSECONDS.toMillis((long) metrics().getAverageGetTime())).tags(getTagsWithCacheName())
                .description("Average get time").register(registry);
        Gauge.builder("cache.puts.averagetime", cacheName, cache -> MICROSECONDS.toMillis((long) metrics().getAveragePutTime())).tags(getTagsWithCacheName())
                .description("Average get time").register(registry);
    }

}
