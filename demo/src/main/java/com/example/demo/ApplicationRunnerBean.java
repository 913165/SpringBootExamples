package com.example.demo;

import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.Ignition;
import org.apache.ignite.configuration.IgniteConfiguration;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import javax.cache.Cache;
import javax.cache.Cache.Entry;
import java.util.Iterator;

@Component
public class ApplicationRunnerBean implements ApplicationRunner {
    private static Iterator<Cache.Entry<Integer, String>> iterator;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        System.out.println("ApplicationRunnerBean : start");
        Ignite ignite  = Ignition.start("config/helloworld-ignite.xml");
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
        System.out.println("ApplicationRunnerBean : end");
    }
}
