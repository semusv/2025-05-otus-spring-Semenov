package ru.otus.hw.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.otus.hw.domain.LogAnalysisResult;
import ru.otus.hw.domain.LogLevel;
import ru.otus.hw.domain.Notification;


@Slf4j
@Service
public class NotificationServiceImpl implements NotificationService {

    @Override
    public Notification generateNotification(LogAnalysisResult analysis) {
        if (analysis.needsNotification()) {
            String message = String.format("ALERT: %s - %s - %s",
                    analysis.component(), analysis.level(), analysis.pattern());

            String recipient = analysis.level() == LogLevel.CRITICAL ?
                    "on-call-team" : "dev-team";

            log.warn("Generating notification: {}", message);
            var notification = new Notification(message, analysis.level(), recipient);
            log.info(notification.toString());
            return notification;
        }
        return null;
    }
}
