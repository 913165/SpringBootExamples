package com.example.demo.init;

import com.example.demo.metrics.IgniteCacheMetrics;
import io.micrometer.core.instrument.MeterRegistry;
import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.IgniteException;
import org.apache.ignite.Ignition;
import org.apache.ignite.configuration.IgniteConfiguration;
import org.apache.ignite.spi.metric.jmx.JmxMetricExporterSpi;
import org.apache.ignite.spi.metric.sql.SqlViewMetricExporterSpi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import javax.cache.Cache;
import javax.cache.Cache.Entry;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Component
@ConditionalOnClass({MeterRegistry.class})
@ConditionalOnProperty(prefix = "ignite.metrics", name = "enabled", havingValue = "true", matchIfMissing = true)
public class ApplicationRunnerBean implements ApplicationRunner {
    private static Iterator<Cache.Entry<Integer, String>> iterator;

    @Autowired
    MeterRegistry metricsRegistry;

    @Value("${ignite.metrics.frequency:5000}")
    Integer metricsFrequencyMillis = 5000;

    private final List<IgniteCacheMetrics> metrics = new ArrayList<>();

    @Override
    public void run(ApplicationArguments args) throws Exception {
        System.out.println("ApplicationRunnerBean : start");
        Ignite ignite = Ignition.start("config/helloworld-ignite.xml");
        System.out.println("igniteControllerTest():Start");
        IgniteCache<Integer, String> cache = ignite.getOrCreateCache("empCache");
        cache.put(101, "John Smith");
        cache.put(102, "Juan Carlos");
        cache.put(103, "Mike Jones");
        cache.put(104, "William jack");
        cache.put(105, "Thomas Harry");
        iterator = cache.iterator();
        while (iterator.hasNext()) {
            Entry<Integer, String> entry = iterator.next();
            System.out.println("Key : " + entry.getKey() + " :Value : " + entry.getValue());
        }

        //register cache metrics
        ignite.cacheNames().forEach(cache2 -> {
            ignite.cache(cache2).enableStatistics(true);
            metrics.add(IgniteCacheMetrics.monitor(metricsRegistry, ignite, cache2, metricsFrequencyMillis));
        });
        System.out.println("ApplicationRunnerBean : end");
    }

    public static void main(String[] args) throws IgniteException {
        String srvPortStr = System.getProperty("IGNITE_JETTY_PORT");
        System.setProperty("IGNITE_JETTY_PORT", "8888");
        Ignite ignite = Ignition.start("config/helloworld-ignite.xml");
        IgniteCache<Integer, String> cache = ignite.getOrCreateCache("empCache");
        cache.put(101, "John Smith");
        cache.put(102, "Juan Carlos");
        cache.put(103, "Mike Jones");
        cache.put(104, "William jack");
        cache.put(105, "Thomas Harry");
        iterator = cache.iterator();
        while (iterator.hasNext()) {
            Entry<Integer, String> entry = iterator.next();
            System.out.println("Key : " + entry.getKey() + " :Value : " + entry.getValue());
        }
        IgniteConfiguration cfg = ignite.configuration();
        cfg.setMetricExporterSpi(new JmxMetricExporterSpi(), new SqlViewMetricExporterSpi());
        System.out.println("end1" + srvPortStr);
        System.out.println("end2");
    }
}
