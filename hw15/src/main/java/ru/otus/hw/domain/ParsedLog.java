package ru.otus.hw.domain;

import java.time.LocalDateTime;

public record ParsedLog(String message, LogLevel level, String component, LocalDateTime timestamp) {
}