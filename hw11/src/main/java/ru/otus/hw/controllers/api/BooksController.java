package ru.otus.hw.controllers.api;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.otus.hw.dto.api.BookCreateDto;
import ru.otus.hw.dto.BookDto;
import ru.otus.hw.dto.api.BookUpdateDto;
import ru.otus.hw.services.BookService;

@SuppressWarnings("unused")
@RestController
@RequiredArgsConstructor
public class BooksController {

    private final BookService bookService;

    @GetMapping("/api/books")
    public Flux<BookDto> getAllBooks() {
        return bookService.findAll();
    }

    @GetMapping("/api/books/{id}")
    public Mono<ResponseEntity<BookDto>> getBook(
            @PathVariable("id") Long id) {
        return bookService.findById(id)
                .map(book -> ResponseEntity
                        .status(HttpStatus.OK)
                        .body(book));

    }

    @PostMapping("/api/books")
    public Mono<ResponseEntity<BookDto>> insertBook(
            @Valid @RequestBody BookCreateDto bookCreateDto) {
        return bookService.insert(bookCreateDto)
                .map(savedBook -> ResponseEntity
                        .status(HttpStatus.CREATED)
                        .body(savedBook));
    }


    @PutMapping("api/books/{id}")
    public Mono<ResponseEntity<BookDto>> updateBook(
            @PathVariable("id") Long id,
            @Valid @RequestBody BookUpdateDto bookUpdateDto) {

        return bookService.update(bookUpdateDto)
                .map(savedBook -> ResponseEntity
                        .status(HttpStatus.OK)
                        .body(savedBook));

    }

    @DeleteMapping("/api/books/{id}")
    public Mono<ResponseEntity<Long>> deleteBook(
            @PathVariable("id") Long id) {

        return bookService.deleteById(id)
                .map(idDel -> ResponseEntity
                        .status(HttpStatus.CREATED)
                        .body(idDel));
    }
}
