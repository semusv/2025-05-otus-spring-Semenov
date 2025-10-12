package ru.otus.hw.controllers.api;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.otus.hw.dto.api.BookFormDto;
import ru.otus.hw.dto.BookDto;
import ru.otus.hw.services.BookService;

import java.util.List;

@SuppressWarnings("unused")
@RestController
@RequiredArgsConstructor
@Slf4j
public class BooksController {

    private final BookService bookService;

    private final MessageSource messageSource;


    @CircuitBreaker(name = "getAllBooks")
    @RateLimiter(name = "bookService")
    @GetMapping("/api/books")
    @ResponseStatus(HttpStatus.OK)
    public List<BookDto> getAllBooks() {
        return bookService.findAll();
    }

    @CircuitBreaker(name = "getBook")
    @RateLimiter(name = "bookService")
    @GetMapping("/api/books/{id}")
    @ResponseStatus(HttpStatus.OK)
    public BookDto getBook(
            @PathVariable("id") Long id) {
        return bookService.findById(id);
    }

    @CircuitBreaker(name = "insertBook")
    @RateLimiter(name = "bookService")
    @PostMapping("/api/books")
    @ResponseStatus(HttpStatus.CREATED)
    public BookDto insertBook(
            @Valid @RequestBody BookFormDto bookCreateDto) {
        return bookService.insert(bookCreateDto);
    }

    @CircuitBreaker(name = "updateBook")
    @RateLimiter(name = "bookService")
    @PutMapping("api/books/{id}")
    @ResponseStatus(HttpStatus.OK)
    public BookDto updateBook(
            @PathVariable("id") Long id,
            @Valid @RequestBody BookFormDto bookFormDto) {
        return bookService.update(id, bookFormDto);
    }

    @CircuitBreaker(name = "deleteBook")
    @RateLimiter(name = "bookService")
    @DeleteMapping("/api/books/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteBook(
            @PathVariable("id") Long id) {
        bookService.deleteById(id);
    }
}
