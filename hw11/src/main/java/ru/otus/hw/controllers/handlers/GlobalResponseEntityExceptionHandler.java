package ru.otus.hw.controllers.handlers;

import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.RequestPath;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import reactor.core.publisher.Mono;
import ru.otus.hw.controllers.handlers.dto.ValidationErrorResponse;
import ru.otus.hw.exceptions.EntityNotFoundException;
import ru.otus.hw.formatters.ErrorMessageFormatter;

import java.time.LocalDateTime;
import java.util.Map;

@Slf4j
@RestControllerAdvice(basePackages = "ru.otus.hw.controllers.api")
@Order(Ordered.HIGHEST_PRECEDENCE)
@RequiredArgsConstructor
public class GlobalResponseEntityExceptionHandler
        extends ResponseEntityExceptionHandler {
    private final ErrorMessageFormatter errorMessageFormatter;

    private final MessageSource messageSource;

    private final ValidationExceptionHandler validationExceptionHandler;

    //404
    @ExceptionHandler(EntityNotFoundException.class)
    public Mono<ResponseEntity<Object>> handleEntityNotFound(
            EntityNotFoundException ex,
            ServerWebExchange exchange) {

        return Mono.just(handleError(
                ex,
                exchange.getRequest(),
                HttpStatus.NOT_FOUND,
                ex.getMessageCode(),
                ex.getMessageArgs()));
    }

    //Валидация
    @ExceptionHandler(WebExchangeBindException.class)
    public Mono<ResponseEntity<Object>> handleException(
            WebExchangeBindException ex,
            ServerWebExchange exchange) {
        ResponseEntity<ValidationErrorResponse> responseEntity =
                validationExceptionHandler.handleValidationException(ex, exchange.getRequest());

        return Mono.just(ResponseEntity
                .status(responseEntity.getStatusCode())
                .headers(responseEntity.getHeaders())
                .body(responseEntity.getBody()));
    }


    //500
    @ExceptionHandler(Exception.class)
    protected Mono<ResponseEntity<Object>> handleExceptionInternal(
            @Nullable Exception ex,
            ServerWebExchange exchange) {

        return Mono.just(handleError(
                ex,
                exchange.getRequest(),
                HttpStatus.INTERNAL_SERVER_ERROR,
                "error.internal.server"));
    }

    private ResponseEntity<Object> handleError(
            Exception ex,
            ServerHttpRequest request,
            HttpStatus status,
            String messageCode,
            Object... args) {

        String errorText = messageSource.getMessage(
                messageCode,
                args,
                "Server Error",
                LocaleContextHolder.getLocale());

        logErrorDetails(ex, request, errorText);

        return buildApiErrorResponse(ex, errorText, status, request.getPath());
    }

    private void logErrorDetails(Exception ex, ServerHttpRequest request, String errorText) {
        String formattedLog = errorMessageFormatter.format(ex, request, errorText);
        log.error(formattedLog);
    }

    private ResponseEntity<Object> buildApiErrorResponse(
            Exception ex,
            String errorText,
            HttpStatus status,
            RequestPath requestPath) {

        Map<String, Object> body = Map.of(
                "errorText", errorText,
                "status", status.value(),
                "timestamp", LocalDateTime.now(),
                "path", requestPath.toString(),
                "exception", ex.getClass().getSimpleName()
        );

        return ResponseEntity
                .status(status)
                .contentType(MediaType.APPLICATION_JSON)
                .body(body);
    }
}






