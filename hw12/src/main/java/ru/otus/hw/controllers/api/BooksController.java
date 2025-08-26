package ru.otus.hw.controllers.api;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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
import ru.otus.hw.dto.api.BookCreateDto;
import ru.otus.hw.dto.BookDto;
import ru.otus.hw.dto.api.BookUpdateDto;
import ru.otus.hw.services.BookService;

import java.util.List;

@SuppressWarnings("unused")
@RestController
@RequiredArgsConstructor
public class BooksController {

    private final BookService bookService;

    private final MessageSource messageSource;

    @GetMapping("/api/books")
    @ResponseStatus(HttpStatus.OK)
    public List<BookDto> getAllBooks() {
        return bookService.findAll();
    }

    @GetMapping("/api/books/{id}")
    @ResponseStatus(HttpStatus.OK)
    public BookDto getBook(
            @PathVariable("id") Long id) {
        return bookService.findById(id);
    }

    @PostMapping("/api/books")
    @ResponseStatus(HttpStatus.CREATED)
    public BookDto insertBook(
            @Valid @RequestBody BookCreateDto bookCreateDto) {
        return bookService.insert(bookCreateDto);
    }


    @PutMapping("api/books/{id}")
    @ResponseStatus(HttpStatus.OK)
    public BookDto updateBook(
            @PathVariable("id") Long id,
            @Valid @RequestBody BookUpdateDto bookUpdateDto) {
        return bookService.update(bookUpdateDto);
    }

    @DeleteMapping("/api/books/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteBook(
            @PathVariable("id") Long id) {
        bookService.deleteById(id);
    }
}
