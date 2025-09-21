package ru.otus.hw.indicators;


import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicReference;

@Component
public class DatabaseHealthIndicator implements HealthIndicator {

    private final JdbcTemplate jdbcTemplate;

    private final AtomicReference<Health> lastHealthStatus = new AtomicReference<>();

    public DatabaseHealthIndicator(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Health health() {
        try {
            jdbcTemplate.queryForObject("SELECT 1 FROM DUAL", Integer.class);
            // Проверяем наличие основных таблиц
            boolean booksTableExists = checkTableExists("BOOKS");
            boolean authorsTableExists = checkTableExists("AUTHORS");
            boolean genresTableExists = checkTableExists("GENRES");

            Health health = Health.up()
                    .withDetail("database", "H2 In-Memory")
                    .withDetail("status", "connected")
                    .withDetail("timestamp", LocalDateTime.now())
                    .withDetail("tables.books", booksTableExists ? "exists" : "missing")
                    .withDetail("tables.authors", authorsTableExists ? "exists" : "missing")
                    .withDetail("tables.genres", genresTableExists ? "exists" : "missing")
                    .build();
            lastHealthStatus.set(health);
            return health;
        } catch (Exception e) {
            Health health = Health.down()
                    .withDetail("database", "H2 In-Memory")
                    .withDetail("status", "connection failed")
                    .withDetail("error", e.getMessage())
                    .withDetail("timestamp", LocalDateTime.now())
                    .withDetail("exception_type", e.getClass().getSimpleName())
                    .build();
            lastHealthStatus.set(health);
            return health;
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

    public Health getLastStatus() {
        return lastHealthStatus.get();
    }
}