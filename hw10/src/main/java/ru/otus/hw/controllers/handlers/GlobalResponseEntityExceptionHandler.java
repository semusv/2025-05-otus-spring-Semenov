package ru.otus.hw.controllers.handlers;

import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import ru.otus.hw.controllers.handlers.dto.ValidationErrorResponse;
import ru.otus.hw.exceptions.EntityNotFoundException;
import ru.otus.hw.formatters.ErrorMessageFormatter;

import java.time.LocalDateTime;
import java.util.Map;


@Slf4j
@RestControllerAdvice(basePackages = "ru.otus.hw.controllers.api")
@Order(Ordered.HIGHEST_PRECEDENCE)
@RequiredArgsConstructor
public class GlobalResponseEntityExceptionHandler extends ResponseEntityExceptionHandler {

    private final ErrorMessageFormatter errorMessageFormatter;

    private final MessageSource messageSource;

    private final ValidationExceptionHandler validationExceptionHandler;

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<Object> handleEntityNotFoundForWeb(
            EntityNotFoundException ex,
            WebRequest request) {

        return handleError(
                ex,
                request,
                HttpStatus.NOT_FOUND,
                ex.getMessageCode(),
                ex.getMessageArgs());
    }

    // Обработка ошибок валидации DTO
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            @Nullable MethodArgumentNotValidException ex,
            @Nullable HttpHeaders headers,
            @Nullable HttpStatusCode status,
            @Nullable WebRequest request) {

        if (ex == null) {
            return null;
        }

        ResponseEntity<ValidationErrorResponse> responseEntity =
                validationExceptionHandler.handleValidationException(ex, request);

        return ResponseEntity
                .status(responseEntity.getStatusCode())
                .headers(responseEntity.getHeaders())
                .body(responseEntity.getBody());
    }

    @Override
    protected ResponseEntity<Object> handleExceptionInternal(
            @Nullable Exception ex,
            Object body,
            @Nullable HttpHeaders headers,
            HttpStatusCode statusCode,
            @Nullable WebRequest request) {

        return handleError(
                ex,
                request,
                HttpStatus.valueOf(statusCode.value()),
                "error.internal.server");
    }

    private ResponseEntity<Object> handleError(
            Exception ex,
            WebRequest request,
            HttpStatus status,
            String messageCode,
            Object... args) {

        String errorText = messageSource.getMessage(
                messageCode,
                args,
                "Server Error",
                LocaleContextHolder.getLocale()
        );

        logErrorDetails(ex, request, errorText);

        return buildApiErrorResponse(ex, errorText, status, request);
    }

    private void logErrorDetails(Exception ex, WebRequest request, String errorText) {
        String formattedLog = errorMessageFormatter.format(ex, request, errorText);
        log.error(formattedLog);
    }

    private ResponseEntity<Object> buildApiErrorResponse(
            Exception ex,
            String errorText,
            HttpStatus status,
            WebRequest request) {

        Map<String, Object> body = Map.of(
                "errorText", errorText,
                "status", status.value(),
                "timestamp", LocalDateTime.now(),
                "path", ((ServletWebRequest) request).getRequest().getRequestURI(),
                "exception", ex.getClass().getSimpleName()
        );

        return ResponseEntity
                .status(status)
                .contentType(MediaType.APPLICATION_JSON)
                .body(body);
    }
}






