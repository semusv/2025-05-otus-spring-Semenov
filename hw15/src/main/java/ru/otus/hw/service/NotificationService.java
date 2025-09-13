package ru.otus.hw.service;

import ru.otus.hw.domain.LogAnalysisResult;
import ru.otus.hw.domain.Notification;

@SuppressWarnings("unused")
public interface NotificationService {
    Notification generateNotification(LogAnalysisResult analysis);
}
