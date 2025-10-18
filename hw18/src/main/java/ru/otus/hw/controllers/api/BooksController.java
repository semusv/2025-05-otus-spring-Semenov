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
import org.springframework.web.server.ResponseStatusException;
import ru.otus.hw.dto.api.BookFormDto;
import ru.otus.hw.dto.BookDto;
import ru.otus.hw.services.BookService;

import java.lang.reflect.Method;
import java.util.List;

@SuppressWarnings("unused")
@RestController
@RequiredArgsConstructor
@Slf4j
public class BooksController {

    private final BookService bookService;

    private final MessageSource messageSource;


    @CircuitBreaker(name = "readOperations", fallbackMethod = "fallbackForGetAll")
    @RateLimiter(name = "bookService")
    @GetMapping("/api/books")
    @ResponseStatus(HttpStatus.OK)
    public List<BookDto> getAllBooks() {
         return bookService.findAll();
    }

    @CircuitBreaker(name = "readOperations", fallbackMethod = "fallbackForGetBook")
    @RateLimiter(name = "bookService")
    @GetMapping("/api/books/{id}")
    @ResponseStatus(HttpStatus.OK)
    public BookDto getBook(
            @PathVariable("id") Long id) {
        return bookService.findById(id);
    }

    @CircuitBreaker(name = "writeOperations", fallbackMethod = "fallbackForWrite")
    @RateLimiter(name = "bookService")
    @PostMapping("/api/books")
    @ResponseStatus(HttpStatus.CREATED)
    public BookDto insertBook(
            @Valid @RequestBody BookFormDto bookCreateDto) {
        return bookService.insert(bookCreateDto);
    }

    @CircuitBreaker(name = "writeOperations", fallbackMethod = "fallbackForUpdate")
    @RateLimiter(name = "bookService")
    @PutMapping("api/books/{id}")
    @ResponseStatus(HttpStatus.OK)
    public BookDto updateBook(
            @PathVariable("id") Long id,
            @Valid @RequestBody BookFormDto bookFormDto) {
        return bookService.update(id, bookFormDto);
    }

    @CircuitBreaker(name = "writeOperations", fallbackMethod = "fallbackForDelete")
    @RateLimiter(name = "bookService")
    @DeleteMapping("/api/books/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteBook(
            @PathVariable("id") Long id) {
        bookService.deleteById(id);
    }


    public List<BookDto> fallbackForGetAll(Exception ex) {
        log.warn("Fallback: returning empty list", ex);
        return List.of();
    }

    public BookDto fallbackForGetBook(Long id, Exception ex) {
        log.warn("Fallback: book not found for id {}", id, ex);
        return null;
    }

    public BookDto fallbackForWrite(BookFormDto dto, Exception ex) {
        log.error("Fallback: write operation failed", ex);
        throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Service temporarily unavailable");
    }

    public BookDto fallbackForUpdate(Long id, BookFormDto dto, Exception ex) {
        log.error("Fallback: update operation failed for id {}", id, ex);
        throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Update operation temporarily unavailable");
    }

    public void fallbackForDelete(Long id, Exception ex) {
        log.error("Fallback: delete operation failed for id {}", id, ex);
        throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Delete operation temporarily unavailable");
    }

}
