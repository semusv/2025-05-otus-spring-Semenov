package ru.otus.hw.controllers.api;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.otus.hw.dto.GenreDto;
import ru.otus.hw.services.GenreService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
public class GenresController {
    private final GenreService genreService;

    @CircuitBreaker(name = "readOperations", fallbackMethod = "fallbackForGetAll")
    @RateLimiter(name = "GenreService")
    @GetMapping("/api/genres")
    @ResponseStatus(HttpStatus.OK)
    public List<GenreDto> getAllGenres() {
        return genreService.findAll();
    }

    public List<GenreDto> fallbackForGetAll(Exception ex) {
        log.warn("Fallback: returning empty list", ex);
        return List.of();
    }
}