package ru.otus.hw.integration;


import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.integration.test.context.SpringIntegrationTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import ru.otus.hw.config.AppRunner;
import ru.otus.hw.domain.Notification;
import ru.otus.hw.domain.RawLog;
import ru.otus.hw.service.LogProcessingGateway;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@SpringIntegrationTest
class LogIntegrationFlowTest {

    @SuppressWarnings("unused")
    @MockitoBean
    private AppRunner appRunner;

    @Autowired
    private LogProcessingGateway logProcessingGateway;

    @Test
    @DisplayName("Проверяем результат работы flow")
    void testLogProcessingFlow() {
        // Given
        List<RawLog> rawLogs = List.of(
                new RawLog("ERROR database connection timeout", LocalDateTime.now(), "db-service"),
                new RawLog("INFO user login successful", LocalDateTime.now(), "auth-service"),
                new RawLog("CRITICAL system shutdown", LocalDateTime.now(), "core-service")
        );

        // When
        List<Notification> notifications = logProcessingGateway.processLogs(rawLogs);

        // Then
        assertNotNull(notifications, "Notifications should not be null");
        assertEquals(2, notifications.size(), "Should generate at least one notification for ERROR/CRITICAL logs");

        notifications.forEach(notification -> {
            assertNotNull(notification.message());
            assertNotNull(notification.severity());
            assertNotNull(notification.recipient());
        });
    }

    @Test
    @DisplayName("Просто проверяем что flow отработал без ошибок")
    void testFlowWithDirectChannelAccess() {
        // Given
        List<RawLog> rawLogs = List.of(
                new RawLog("ERROR file not found", LocalDateTime.now(), "file-service"),
                new RawLog("WARN memory usage high", LocalDateTime.now(), "monitoring-service")
        );

        // When
        List<Notification> notifications = logProcessingGateway.processLogs(rawLogs);

        // Then -
        assertNotNull(notifications);
    }

    @Test
    @DisplayName("Только INFO логи которые должны быть отфильтрованы")
    void testNoNotificationsForInfoLogs() {
        // Given -
        List<RawLog> rawLogs = List.of(
                new RawLog("INFO request started", LocalDateTime.now(), "api-gateway"),
                new RawLog("INFO request completed", LocalDateTime.now(), "api-gateway"),
                new RawLog("DEBUG detailed info", LocalDateTime.now(), "debug-service")
        );

        // When
        List<Notification> notifications = logProcessingGateway.processLogs(rawLogs);

        // Then - может вернуть null или пустой список в зависимости от реализации
        assertTrue(notifications == null || notifications.isEmpty(),
                "Should not generate notifications for INFO/DEBUG logs");
    }
}