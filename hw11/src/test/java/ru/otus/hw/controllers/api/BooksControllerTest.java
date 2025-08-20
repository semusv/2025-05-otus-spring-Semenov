package ru.otus.hw.controllers.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.context.annotation.Import;
import org.springframework.context.MessageSource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.otus.hw.controllers.handlers.GlobalExceptionHandler;
import ru.otus.hw.controllers.handlers.GlobalResponseEntityExceptionHandler;
import ru.otus.hw.controllers.handlers.ValidationExceptionHandler;
import ru.otus.hw.dto.AuthorDto;
import ru.otus.hw.dto.BookDto;
import ru.otus.hw.dto.GenreDto;
import ru.otus.hw.dto.api.BookCreateDto;
import ru.otus.hw.dto.api.BookUpdateDto;
import ru.otus.hw.exceptions.EntityNotFoundException;
import ru.otus.hw.formatters.ErrorMessageFormatter;
import ru.otus.hw.services.AuthorService;
import ru.otus.hw.services.BookService;
import ru.otus.hw.services.CommentService;
import ru.otus.hw.services.GenreService;

import java.util.List;
import java.util.Locale;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.reactive.server.WebTestClient.ResponseSpec;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SuppressWarnings("unused")
@WebFluxTest(controllers = BooksController.class)

@Import({GlobalExceptionHandler.class,
        GlobalResponseEntityExceptionHandler.class,
        ValidationExceptionHandler.class})

class BooksControllerTest {

    private static final String API_URL = "/api/books";

    @Autowired
    private WebTestClient webTestClient;
    @Autowired
    private ObjectMapper mapper;
    @MockitoBean
    private BookService bookService;
    @MockitoBean
    private CommentService commentService;
    @MockitoBean
    private GenreService genreService;
    @MockitoBean
    private AuthorService authorService;
    @MockitoBean
    private MessageSource messageSource;
    @MockitoBean
    @Autowired
    private ErrorMessageFormatter errorMessageFormatter;

    @Test
    @DisplayName("GET /api/books - возвращает список всех книг")
    void shouldReturnBooksList() throws Exception {
        List<BookDto> expectedBooks = List.of(
                new BookDto(1L, "Книга 1",
                        new AuthorDto(1L, "Автор 1"),
                        List.of(new GenreDto(1L, "Жанр 1"))),
                new BookDto(2L, "Книга 2",
                        new AuthorDto(2L, "Автор 2"),
                        List.of(new GenreDto(2L, "Жанр 2"))));


        when(bookService.findAll()).thenReturn(Flux.fromIterable(expectedBooks));

        ResponseSpec rs = webTestClient.get()
                .uri(API_URL)
                .accept(APPLICATION_JSON)
                .exchange();

        rs.expectStatus().isOk()
                .expectHeader().contentType(APPLICATION_JSON)
                .expectBody()
                .json(mapper.writeValueAsString(expectedBooks));
    }


    @Test
    @DisplayName("GET /api/books/{id} - возвращает развёрнутый объект книги")
    void shouldReturnBook() throws Exception {
        long bookId = 1L;
        BookDto bookDto = new BookDto(
                bookId,
                "Книга",
                new AuthorDto(1L, "Автор"),
                List.of(new GenreDto(1L, "Жанр")));

        when(bookService.findById(bookId)).thenReturn(Mono.just(bookDto));

        webTestClient.get()
                .uri("/api/books/{id}", bookId)
                .accept(APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(APPLICATION_JSON)
                .expectBody()
                .json(mapper.writeValueAsString(bookDto));
    }

    @Test
    @DisplayName("POST /api/books - создаёт книгу и возвращает ResponseDto")
    void shouldCreateAndReturnResponse() {
        BookCreateDto formDto = new BookCreateDto(
                0L, "Новая книга", 1L, Set.of(1L));
        BookDto created = new BookDto(
                99L,
                formDto.title(),
                new AuthorDto(formDto.authorId(), "Автор 1"),
                List.of(new GenreDto(1L, "Жанр 1")));

        when(bookService.insert(formDto)).thenReturn(Mono.just(created));
        when(messageSource.getMessage(eq("api.response.ok.save.book"),
                any(Object[].class), any(Locale.class)))
                .thenReturn("Book saved");

        webTestClient.post()
                .uri(API_URL)
                .contentType(APPLICATION_JSON)
                .bodyValue(formDto)
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().contentType(APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.id").isEqualTo(created.id().intValue())
                .jsonPath("$.title").isEqualTo(created.title())
                .jsonPath("$.author.id").isEqualTo(created.author().id().intValue())
                .jsonPath("$.genres[0].id").isEqualTo(created.genres().get(0).id().intValue());

        verify(bookService, times(1)).insert(formDto);
    }


    @Test
    @DisplayName("PUT /api/books/{id} - обновляет книгу и возвращает ResponseDto")
    void shouldUpdateAndReturnResponse() {
        long id = 10L;
        BookUpdateDto updateDto = new BookUpdateDto(
                id,
                "Обновлённая книга",
                2L,
                Set.of(3L, 4L));
        BookDto updated = new BookDto(
                id,
                updateDto.title(),
                new AuthorDto(updateDto.authorId(), "Автор 2"),
                List.of(
                        new GenreDto(3L, "Жанр 3"),
                        new GenreDto(4L, "Жанр 4")));

        when(bookService.update(updateDto)).thenReturn(Mono.just(updated));
        when(messageSource.getMessage(eq("api.response.ok.save.book"),
                any(Object[].class), any(Locale.class)))
                .thenReturn("Book updated");

        webTestClient.put()
                .uri("/api/books/{id}", id)
                .contentType(APPLICATION_JSON)
                .bodyValue(updateDto)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.id").isEqualTo(updated.id().intValue())
                .jsonPath("$.title").isEqualTo(updated.title())
                .jsonPath("$.author.id").isEqualTo(updated.author().id().intValue())
                .jsonPath("$.genres").value(Matchers.hasSize(updated.genres().size()));

        verify(bookService, times(1)).update(updateDto);
    }

    @Test
    @DisplayName("DELETE /api/books/{id} - удаляет книгу и возвращает ResponseDto")
    void shouldDeleteAndReturnResponse() throws Exception {
        long id = 55L;
        when(messageSource.getMessage(eq("api.response.ok.delete.book"),
                any(Object[].class), any(Locale.class)))
                .thenReturn("Book deleted");

        when(bookService.deleteById(id)).thenReturn(Mono.empty());

        webTestClient.delete()
                .uri("/api/books/{id}", id)
                .exchange()
                .expectStatus().isNoContent();

        verify(bookService, times(1)).deleteById(id);
    }


    @Test
    @DisplayName("POST - возвращает 400, когда пустые поля")
    void shouldReturnResponse400WhenEmptyFields() {
        BookCreateDto formDto = new BookCreateDto(
                0L, "Новая книга", null, Set.of(1L));

        webTestClient.post()
                .uri(API_URL)
                .contentType(APPLICATION_JSON)
                .bodyValue(formDto)
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    @DisplayName("GET /api/books/{id} - возвращает 404, когда некорректный ID книги")
    void shouldReturnResponse404WhenWrongBookId() {
        long bookId = 999L;
        when(bookService.findById(bookId))
                .thenReturn(Mono.error(new EntityNotFoundException(
                        "Book with id %d not found".formatted(bookId),
                        "exception.entity.not.found.book",
                        bookId)));

        when(messageSource.getMessage(
                any(String.class), any(Object[].class), any(String.class), any(Locale.class)))
                .thenReturn("Not Found Book with ID");

        webTestClient.get()
                .uri("/api/books/{id}", bookId)
                .accept(APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound();
    }


    @Test
    @DisplayName("POST /api/books - возвращает 500, когда некорректный путь")
    void shouldReturnResponse500WhenWrongUrl() {
        BookCreateDto formDto = new BookCreateDto(
                0L, "Новая книга", 1L, Set.of(1L));

        webTestClient.post()
                .uri("/api/wrongUrl")
                .contentType(APPLICATION_JSON)
                .bodyValue(formDto)
                .exchange()
                .expectStatus().is5xxServerError();
    }
}