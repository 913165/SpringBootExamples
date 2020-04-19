package com.example.demo;

import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.Ignition;
import org.apache.ignite.MemoryMetrics;
import org.apache.ignite.configuration.IgniteConfiguration;
import org.apache.ignite.configuration.MemoryPolicyConfiguration;
import org.apache.ignite.internal.managers.systemview.GridSystemViewManager;
import org.apache.ignite.spi.metric.MetricExporterSpi;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.cache.Cache.Entry;
import javax.cache.Cache;
import java.sql.*;
import java.util.Collection;
import java.util.Iterator;

@RestController
public class IgniteContoller {
    private static Iterator<Cache.Entry<Integer, String>> iterator;

    @GetMapping
    public String welcome() {
        return "Welcome to new Spring boot Application";
    }

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
        Collection<MemoryMetrics> regionsMetrics = ignite.memoryMetrics();
        System.out.println("ignite.configuration().getMetricExporterSpi() :" + ignite.configuration().getMetricExporterSpi());
        MetricExporterSpi[] metricExporterSpi = ignite.configuration().getMetricExporterSpi();
        System.out.println("metricExporterSpi.length : " + metricExporterSpi.length);
        for (MetricExporterSpi metricExporterSpi1 : metricExporterSpi) {
            System.out.println("metricExporterSpi1 " + metricExporterSpi1);
        }
        System.out.println("ignite.configuration().getSqlQueryHistorySize() : " + ignite.configuration().getSqlQueryHistorySize());
        // Print some of the metrics' probes for all the regions.
        for (MemoryMetrics metrics : regionsMetrics) {
            System.out.println(">>> Memory Region Name: " + metrics.getName());
            System.out.println(">>> Allocation Rate: " + metrics.getAllocationRate());
            System.out.println(">>> Fill Factor: " + metrics.getPagesFillFactor());
        }
        System.out.println("igniteControllerTest():end");
        cache = ignite.getOrCreateCache("empCache");
        return "igniteControllerTest1";
    }

    @GetMapping("test2")
    public String igniteControllerTest2() throws SQLException {
        System.out.println("igniteControllerTest():Start");
        try (Connection conn = DriverManager.getConnection("jdbc:ignite:thin://127.0.0.1/")) {
            try (Statement stmt = conn.createStatement()) {
                // try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM Salaries")) {
                try (ResultSet rs = stmt.executeQuery("SELECT * FROM Salaries where id =1")) {
                    rs.next();
                    //System.out.println("Populated Salary table: " + rs.getLong(1) + " entries");
                    System.out.println("Employee Name: " + rs.getString(2) + " ");
                    System.out.println("Job Title: " + rs.getString(3) + "");
                }
            }
        }

        return "igniteControllerTest1";
    }

    private static void executeCommand(Connection conn, String sql) throws Exception {
        try (Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(sql);
        }
    }

    public static String systemSchemaName() {
        return "SYS";
    }

    @GetMapping("test3")
    public String igniteControllerTest3() throws SQLException {
        System.out.println("igniteControllerTest():Start");
        try (Connection conn = DriverManager.getConnection("jdbc:ignite:thin://127.0.0.1/")) {
            String sqlHist = "SELECT SCHEMA_NAME, SQL, LOCAL, EXECUTIONS, FAILURES, DURATION_MIN, DURATION_MAX, LAST_START_TIME " +
                    "FROM " + systemSchemaName() + ".SQL_QUERIES_HISTORY ORDER BY LAST_START_TIME DESC LIMIT 0, 3";
            try (Statement stmt = conn.createStatement()) {
                try (ResultSet rs = stmt.executeQuery(sqlHist)) {
                    while (rs.next()) {
                        //System.out.println("SCHEMA_NAME: " + rs.getString("SCHEMA_NAME"));
                        System.out.println("SQL: " + rs.getString("SQL"));
                        //System.out.println("LOCAL: " + rs.getString("LOCAL"));
                        System.out.println("EXECUTIONS: " + rs.getString("EXECUTIONS"));
                        //System.out.println("FAILURES: " + rs.getString("FAILURES"));
                        //System.out.println("DURATION_MIN: " + rs.getString("DURATION_MIN"));
                        //System.out.println("DURATION_MAX: " + rs.getString("DURATION_MAX"));
                        //System.out.println("LAST_START_TIME: " + rs.getString("LAST_START_TIME"));
                    }
                }
            }
        }

        return "igniteControllerTest1";
    }
}
