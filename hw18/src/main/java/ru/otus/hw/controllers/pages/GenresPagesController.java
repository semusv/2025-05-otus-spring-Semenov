package ru.otus.hw.controllers.pages;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;


@SuppressWarnings("SameReturnValue")
@Controller
@RequiredArgsConstructor
public class GenresPagesController {

    @GetMapping("/genres")
    public String getAll() {
        return "genres-list";
    }
}
