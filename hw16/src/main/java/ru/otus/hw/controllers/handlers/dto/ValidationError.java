package ru.otus.hw.controllers.handlers.dto;

import jakarta.annotation.Nullable;

public record ValidationError(
        @Nullable String field,
        String message,
        @Nullable Object rejectedValue
) {
}
