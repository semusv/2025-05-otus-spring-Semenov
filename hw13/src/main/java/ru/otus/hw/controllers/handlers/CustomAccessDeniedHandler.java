package ru.otus.hw.controllers.handlers;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
@RequiredArgsConstructor
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    private final MessageSource messageSource;

    @Override
    public void handle(HttpServletRequest request,
                       HttpServletResponse response,
                       AccessDeniedException accessDeniedException)
            throws IOException {

        if (request.getUserPrincipal() != null) {
            System.out.println("User: " + request.getUserPrincipal().getName());
        }
        System.out.println("Requested URL: " + request.getRequestURI());
        System.out.println("Exception: " + accessDeniedException.getMessage());

        handleWebRequest(response);
    }


    private void handleWebRequest(HttpServletResponse response) throws IOException {
        response.sendRedirect("/access-denied");
    }

}
