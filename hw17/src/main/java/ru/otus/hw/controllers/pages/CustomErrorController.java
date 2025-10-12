package ru.otus.hw.controllers.pages;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@SuppressWarnings("unused")
@Slf4j
@RequiredArgsConstructor
public class CustomErrorController implements ErrorController {

    private final MessageSource messageSource;

    @RequestMapping
    public String handleError(Model model, HttpServletRequest request) {
        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        model.addAttribute("status", status);

        String errorText = messageSource.getMessage("error.internal.server", null,
                LocaleContextHolder.getLocale());

        Throwable ex = (Throwable) request.getAttribute(RequestDispatcher.ERROR_EXCEPTION);
        if (ex != null) {
            model.addAttribute("errorText", errorText);
            model.addAttribute("requestId", request.getAttribute("requestId"));
            log.error("Page error", ex);
        }

        return "customError";
    }
}
