package ru.otus.hw.formatters;

import org.springframework.http.server.reactive.ServerHttpRequest;

public interface ErrorMessageFormatter {
    String format(Exception ex, ServerHttpRequest request, String errorText);
}
