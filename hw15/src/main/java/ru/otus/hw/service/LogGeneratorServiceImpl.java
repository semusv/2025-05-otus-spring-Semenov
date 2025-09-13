package ru.otus.hw.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.otus.hw.domain.Notification;
import ru.otus.hw.domain.RawLog;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ForkJoinPool;

@Slf4j
@Service
@RequiredArgsConstructor
public class LogGeneratorServiceImpl implements LogGeneratorService {


    private static final String[] COMPONENTS = {"auth-service", "db-service", "api-gateway", "cache-service"};

    private static final String[] LOG_MESSAGES = {
            "ERROR database connection timeout",
            "INFO user login successful",
            "WARN memory usage high",
            "CRITICAL system shutdown imminent",
            "DEBUG processing request",
            "ERROR file not found",
            "INFO request completed"
    };

    private final LogProcessingGateway logProcessingGateway;

    private final Random random = new Random();

    @Override
    public void startLogGeneration() {
        ForkJoinPool pool = ForkJoinPool.commonPool();

        for (int i = 0; i < 5; i++) {
            log.info("----------------------");
            int batchNumber = i + 1;
            pool.execute(() -> {
                List<RawLog> logs = generateLogBatch();
                log.info("Batch {}: Generated {} logs", batchNumber, logs.size());
                log.info(logs.toString());
                List<Notification> notifications = logProcessingGateway.processLogs(logs);

                log.info("+++++++++++++++++++++++");
                log.info("Batch {}: Generated {} notifications", batchNumber, notifications.size());
                notifications.forEach(notification ->
                        log.info("Notification: {}", notification.message()));
            });

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    private List<RawLog> generateLogBatch() {

        List<RawLog> logs = new ArrayList<>();

        int count = random.nextInt(10) + 5; // 5-15 логов в батче

        for (int i = 0; i < count; i++) {
            String logMessage = LOG_MESSAGES[random.nextInt(LOG_MESSAGES.length)];
            String component = COMPONENTS[random.nextInt(COMPONENTS.length)];

            logs.add(new RawLog(
                    logMessage,
                    LocalDateTime.now(),
                    component
            ));
        }

        return logs;
    }
}