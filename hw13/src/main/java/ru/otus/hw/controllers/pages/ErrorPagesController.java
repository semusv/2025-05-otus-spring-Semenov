package ru.otus.hw.controllers.pages;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ErrorPagesController {

    @GetMapping("/access-denied")
    public String handleAccessDenied() {
        return "access-denied";
    }

}
