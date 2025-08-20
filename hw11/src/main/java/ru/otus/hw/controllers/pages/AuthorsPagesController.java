package ru.otus.hw.controllers.pages;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;


@SuppressWarnings("SameReturnValue")
@Controller
@RequiredArgsConstructor
public class AuthorsPagesController {

    @GetMapping("/authors")
    public String getAll() {

        return "authors-list";
    }
}

