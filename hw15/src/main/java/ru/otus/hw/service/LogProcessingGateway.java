package ru.otus.hw.service;

import org.springframework.integration.annotation.Gateway;
import org.springframework.integration.annotation.MessagingGateway;
import ru.otus.hw.domain.Notification;
import ru.otus.hw.domain.RawLog;

import java.util.List;

@MessagingGateway
public interface LogProcessingGateway {
    @Gateway(requestChannel = "rawLogsChannel", replyChannel = "notificationsChannel")
    List<Notification> processLogs(List<RawLog> rawLogs);
}