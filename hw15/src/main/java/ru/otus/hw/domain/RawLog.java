package ru.otus.hw.domain;

import java.time.LocalDateTime;

public record RawLog(String logLine, LocalDateTime timestamp, String source) {
}

