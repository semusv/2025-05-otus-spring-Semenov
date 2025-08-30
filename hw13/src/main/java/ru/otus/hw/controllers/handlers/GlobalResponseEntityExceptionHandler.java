package ru.otus.hw.controllers.handlers;

import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import ru.otus.hw.controllers.handlers.dto.ValidationErrorResponse;
import ru.otus.hw.exceptions.EntityNotFoundException;
import ru.otus.hw.services.ErrorHandlingService;


@Slf4j
@RestControllerAdvice(basePackages = "ru.otus.hw.controllers.api")
@Order(Ordered.HIGHEST_PRECEDENCE)
@RequiredArgsConstructor
public class GlobalResponseEntityExceptionHandler extends ResponseEntityExceptionHandler {

    private final ValidationExceptionHandler validationExceptionHandler;

    private final ErrorHandlingService errorHandlingService;

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<Object> handleEntityNotFoundForWeb(
            EntityNotFoundException ex,
            WebRequest request) {

        return errorHandlingService.handleError(
                ex,
                request,
                HttpStatus.NOT_FOUND,
                ex.getMessageCode(),
                ex.getMessageArgs());

    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Object> handleAccessDeniedException(
            AccessDeniedException ex,
            WebRequest request) {

        return errorHandlingService.handleError(
                ex,
                request,
                HttpStatus.NOT_FOUND,
                "error.access.denied",
                (Object[]) null);

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

    //500
    @Override
    protected ResponseEntity<Object> handleExceptionInternal(
            @Nullable Exception ex,
            Object body,
            @Nullable HttpHeaders headers,
            HttpStatusCode statusCode,
            @Nullable WebRequest request) {

        return errorHandlingService.handleError(
                ex,
                request,
                HttpStatus.valueOf(statusCode.value()),
                "error.internal.server");
    }
}






