package ru.otus.hw.services;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.WebRequest;

import java.util.Map;

public interface  ErrorHandlingService {

    ResponseEntity<Object> handleError(
            Exception ex,
            WebRequest request,
            HttpStatus status,
            String messageCode,
            Object... args);

    ResponseEntity<Object> handleError(
            Exception ex,
            HttpServletRequest request,
            HttpStatus status,
            String messageCode,
            Object... args);

}
