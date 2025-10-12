package ru.otus.hw.controllers.handlers.dto;

import java.time.LocalDateTime;
import java.util.List;

public record ValidationErrorResponse(
        String errorType,
        int status,
        LocalDateTime timestamp,
        String path,
        List<ValidationError> errors
) {
}
