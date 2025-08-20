package ru.otus.hw.controllers.handlers;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.server.ServerWebExchange;
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
public class GlobalResponseEntityExceptionHandler
        extends ResponseEntityExceptionHandler {
    private final ErrorMessageFormatter errorMessageFormatter;

    private final MessageSource messageSource;

    private final ValidationExceptionHandler validationExceptionHandler;

    //404
    @ExceptionHandler(EntityNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String, Object> handleEntityNotFound(
            EntityNotFoundException ex,
            ServerWebExchange exchange) {
        return handleError(
                ex,
                exchange.getRequest(),
                ex.getMessageCode(),
                ex.getMessageArgs());
    }

    //Валидация
    @ExceptionHandler(WebExchangeBindException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ValidationErrorResponse> handleException(
            WebExchangeBindException ex,
            ServerWebExchange exchange) {
        return validationExceptionHandler.handleValidationException(ex, exchange.getRequest());
    }

    //500
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    protected Map<String, Object> handleExceptionInternal(
            Exception ex,
            ServerWebExchange exchange) {
        return handleError(
                ex,
                exchange.getRequest(),
                "error.internal.server");
    }

    private Map<String, Object> handleError(
            Exception ex,
            ServerHttpRequest request,
            String messageCode,
            Object... args) {

        String errorText = messageSource.getMessage(
                messageCode,
                args,
                "Server Error",
                LocaleContextHolder.getLocale());
        logErrorDetails(ex, request, errorText);
        return Map.of(
                "errorText", errorText != null ? errorText : "Error",
                "timestamp", LocalDateTime.now(),
                "path", request.getPath().toString(),
                "exception", ex.getClass().getSimpleName()
        );

    }

    private void logErrorDetails(Exception ex, ServerHttpRequest request, String errorText) {
        String formattedLog = errorMessageFormatter.format(ex, request, errorText);
        log.error(formattedLog);
    }


}






