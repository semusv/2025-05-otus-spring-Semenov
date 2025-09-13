package ru.otus.hw.domain;

public record Notification(String message, LogLevel severity, String recipient) {
}

