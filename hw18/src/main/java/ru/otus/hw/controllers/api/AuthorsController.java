package ru.otus.hw.controllers.api;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.otus.hw.dto.AuthorDto;
import ru.otus.hw.services.AuthorService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
public class AuthorsController {

    private final AuthorService authorService;

    @CircuitBreaker(name = "readOperations", fallbackMethod = "fallbackForGetAll")
    @RateLimiter(name = "authorService")
    @GetMapping("/api/authors")
    @ResponseStatus(HttpStatus.OK)
    public List<AuthorDto> getAllAuthors() {
        return authorService.findAll();
    }

    public List<AuthorDto> fallbackForGetAll(Exception ex) {
        log.warn("Fallback: returning empty list", ex);
        return List.of();
    }

}
