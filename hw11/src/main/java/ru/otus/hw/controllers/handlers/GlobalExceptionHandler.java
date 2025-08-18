package ru.otus.hw.controllers.handlers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.reactive.result.view.Rendering;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import ru.otus.hw.formatters.ErrorMessageFormatter;

@RequiredArgsConstructor
@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    private final MessageSource messageSource;

    private final ErrorMessageFormatter errorMessageFormatter;

    @ExceptionHandler(Exception.class)
    public Mono<Rendering> handleAllExceptions(Exception ex, ServerWebExchange exchange) {
        String errorText = messageSource.getMessage("error.internal.server", null,
                LocaleContextHolder.getLocale());

        logErrorDetails(ex, exchange.getRequest(), errorText);

        return Mono.just(Rendering.view("customError")
                .modelAttribute("errorText", errorText)
                .modelAttribute("requestId", exchange.getRequest().getId())
                .modelAttribute("exception", ex)
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .build());
    }

    private void logErrorDetails(Exception ex, ServerHttpRequest request, String errorText) {
        String formattedLog = errorMessageFormatter.format(ex, request, errorText);
        log.error(formattedLog);
    }


}



