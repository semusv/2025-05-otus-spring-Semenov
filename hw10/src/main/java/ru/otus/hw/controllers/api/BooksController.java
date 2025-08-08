package ru.otus.hw.controllers.api;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import ru.otus.hw.controllers.api.dto.ApiResponseDto;
import ru.otus.hw.controllers.api.dto.BookCreateDto;
import ru.otus.hw.dto.BookDto;
import ru.otus.hw.controllers.api.dto.BookUpdateDto;
import ru.otus.hw.services.BookService;
import java.util.List;

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
    public ResponseEntity<ApiResponseDto<BookDto>> insertBook(
            @Valid @RequestBody BookCreateDto bookCreateDto) {

        var newBookFormDto = bookService.insert(bookCreateDto);
        return ResponseEntity.ok(
                new ApiResponseDto<>(
                        messageSource.getMessage(
                                "api.response.ok.save.book",
                                new Object[]{newBookFormDto.id()},
                                LocaleContextHolder.getLocale()),
                        newBookFormDto,
                        true));
    }


    @PutMapping("api/books/{id}")
    public ResponseEntity<ApiResponseDto<BookDto>> updateBook(
            @PathVariable("id") Long id,
            @Valid @RequestBody BookUpdateDto bookUpdateDto) {

        var updatedBookDto = bookService.update(bookUpdateDto);
        return ResponseEntity.ok(
                new ApiResponseDto<>(
                        messageSource.getMessage(
                                "api.response.ok.save.book",
                                new Object[]{id},
                                LocaleContextHolder.getLocale()),
                        updatedBookDto,
                        true));
    }

    @DeleteMapping("/api/books/{id}")
    public ResponseEntity<ApiResponseDto<Object>> deleteBook(
            @PathVariable("id") Long id) {

        bookService.deleteById(id);
        return ResponseEntity.ok(
                new ApiResponseDto<>(
                        messageSource.getMessage(
                                "api.response.ok.delete.book",
                                new Object[]{id},
                                LocaleContextHolder.getLocale()),
                        null,
                        true));
    }
}
