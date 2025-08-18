package ru.otus.hw.controllers.pages;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import reactor.core.publisher.Mono;


@SuppressWarnings("SameReturnValue")
@Controller
@RequiredArgsConstructor
public class AuthorsPagesController {

    @GetMapping("/authors")
    public Mono<String> getAll() {

        return Mono.just("authors-list");
    }
}

