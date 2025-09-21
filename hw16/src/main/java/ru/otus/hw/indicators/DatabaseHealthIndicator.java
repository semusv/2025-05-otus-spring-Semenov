package ru.otus.hw.indicators;


import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.LocalDateTime;

@Component
public class DatabaseHealthIndicator implements HealthIndicator {

    private final JdbcTemplate jdbcTemplate;

    public DatabaseHealthIndicator(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Health health() {
        try {
            jdbcTemplate.queryForObject("SELECT 1 FROM DUAL", Integer.class);
            return Health.up()
                    .withDetail("database", "H2 In-Memory")
                    .withDetail("status", "connected")
                    .withDetail("timestamp", LocalDateTime.now())
                    .withDetail("tables.books", checkTableExists("BOOKS") ? "exists" : "missing")
                    .withDetail("tables.authors", checkTableExists("AUTHORS") ? "exists" : "missing")
                    .withDetail("tables.genres", checkTableExists("GENRES") ? "exists" : "missing")
                    .build();
        } catch (Exception e) {
            return Health.down()
                    .withDetail("database", "H2 In-Memory")
                    .withDetail("status", "connection failed")
                    .withDetail("error", e.getMessage())
                    .withDetail("timestamp", LocalDateTime.now())
                    .withDetail("exception_type", e.getClass().getSimpleName())
                    .build();
        }
    }

    private boolean checkTableExists(String tableName) {
        try {
            jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = ?",
                    Integer.class,
                    tableName
            );
            return true;
        } catch (Exception e) {
            return false;
        }
    }

}