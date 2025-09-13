package ru.otus.hw.domain;

public record LogAnalysisResult(String component, LogLevel level, String pattern, boolean needsNotification) {
}
