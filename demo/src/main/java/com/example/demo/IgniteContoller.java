package com.example.demo;

import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.Ignition;
import org.apache.ignite.configuration.IgniteConfiguration;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import javax.cache.Cache.Entry;
import javax.cache.Cache;
import java.util.Iterator;

@RestController
public class IgniteContoller {
    private static Iterator<Cache.Entry<Integer, String>> iterator;
    @GetMapping("test")
    public String igniteControllerTest() {
        System.out.println("igniteControllerTest():Start");
        IgniteConfiguration cfg;
        Ignite ignite = Ignition.ignite();
        IgniteCache<Integer, String> cache = ignite.getOrCreateCache("empCache");
        iterator = cache.iterator();
        while (iterator.hasNext()) {
            Entry<Integer, String> entry = iterator.next();
            System.out.println("Key : " + entry.getKey() + " :Value : " + entry.getValue());
        }
        System.out.println("igniteControllerTest():end");
        return "igniteControllerTest";
    }
}
