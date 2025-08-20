package ru.otus.hw.controllers.api;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import ru.otus.hw.dto.AuthorDto;
import ru.otus.hw.services.AuthorService;


@RestController
@RequiredArgsConstructor
public class AuthorsController {

    private final AuthorService authorService;

    @GetMapping("/api/authors")
    @ResponseStatus(HttpStatus.OK)
    public Flux<AuthorDto> getAllAuthors() {
        return authorService.findAll();
    }
}
