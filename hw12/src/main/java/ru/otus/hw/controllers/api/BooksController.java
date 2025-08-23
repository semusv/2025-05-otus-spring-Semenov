package ru.otus.hw.controllers.api;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
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
    public List<BookDto> getAllBooks() {
        return bookService.findAll();
    }

    @GetMapping("/api/books/{id}")
    public BookDto getBook(
            @PathVariable("id") Long id) {
        return bookService.findById(id);
    }

    @PostMapping("/api/books")
    public ResponseEntity<BookDto> insertBook(
            @Valid @RequestBody BookCreateDto bookCreateDto) {
        var newBookFormDto = bookService.insert(bookCreateDto);
        return ResponseEntity.ok(newBookFormDto);
    }


    @PutMapping("api/books/{id}")
    public ResponseEntity<BookDto> updateBook(
            @PathVariable("id") Long id,
            @Valid @RequestBody BookUpdateDto bookUpdateDto) {

        var updatedBookDto = bookService.update(bookUpdateDto);
        return ResponseEntity.ok(updatedBookDto);
    }

    @DeleteMapping("/api/books/{id}")
    public ResponseEntity<Long> deleteBook(
            @PathVariable("id") Long id) {
        bookService.deleteById(id);
        return ResponseEntity.ok(id);
    }
}
