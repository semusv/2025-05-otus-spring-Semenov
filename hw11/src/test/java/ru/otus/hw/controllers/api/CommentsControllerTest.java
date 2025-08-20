package ru.otus.hw.controllers.api;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import ru.otus.hw.dto.CommentDto;
import ru.otus.hw.formatters.ErrorMessageFormatter;
import ru.otus.hw.services.CommentService;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;

/**
 * Тесты для {@link CommentsController} – реактивный вариант (WebFlux).
 */
@SuppressWarnings("unused")
@WebFluxTest(controllers = CommentsController.class)
@Import({
        GlobalExceptionHandler.class,
        GlobalResponseEntityExceptionHandler.class,
        ValidationExceptionHandler.class
})
class CommentsControllerTest {
    @Autowired
    private WebTestClient webTestClient;
    @Autowired
    private ObjectMapper mapper;
    @MockitoBean
    private CommentService commentService;
    @MockitoBean
    private ErrorMessageFormatter errorMessageFormatter;
    @MockitoBean
    private MessageSource messageSource;

    @Test
    @DisplayName("GET /api/books/{id}/comments – возвращает список комментариев")
    void shouldReturnCommentsForBook() {
        // given
        Long bookId = 1L;
        List<CommentDto> expected = List.of(
                new CommentDto(1L, "First comment", bookId),
                new CommentDto(2L, "Second comment", bookId)
        );

        // when
        when(commentService.findByBookId(bookId))
                .thenReturn(Flux.fromIterable(expected));

        // then
        webTestClient.get()
                .uri("/api/books/{id}/comments", bookId)
                .accept(APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(APPLICATION_JSON)
                .expectBodyList(CommentDto.class)
                .contains(expected.toArray(CommentDto[]::new));
    }

    @Test
    @DisplayName("POST /api/books/{id}/comments – добавление нового комментария")
    void shouldAddCommentToBook() {
        // given
        Long bookId = 5L;
        CommentDto newComment = new CommentDto(null, "Nice book", bookId);
        CommentDto savedComment = new CommentDto(99L, "Nice book", bookId);
        // when
        when(commentService.insert(newComment)).thenReturn(Mono.just(savedComment));

        when(messageSource.getMessage(
                eq("api.response.ok.save.comment"),
                any(Object[].class),
                any(java.util.Locale.class)))
                .thenReturn("Comment saved");

        // then
        webTestClient.post()
                .uri("/api/books/{id}/comments", bookId)
                .contentType(APPLICATION_JSON)
                .bodyValue(newComment)
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().contentType(APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.id").isEqualTo(savedComment.id())
                .jsonPath("$.text").isEqualTo(savedComment.text())
                .jsonPath("$.bookId").isEqualTo(savedComment.bookId());
    }


    @Test
    @DisplayName("DELETE /api/books/{id}/comments/{commentId} – удаление комментария")
    void shouldDeleteCommentFromBook() {
        // given
        Long bookId = 7L;
        long commentId = 123L;

        // when
        when(commentService.deleteById(commentId)).thenReturn(Mono.empty());
        when(messageSource.getMessage(
                eq("api.response.ok.delete.comment"),
                any(Object[].class),
                any(java.util.Locale.class)))
                .thenReturn("Comment deleted");

        // then
        webTestClient.delete()
                .uri("/api/books/{id}/comments/{commentId}", bookId, commentId)
                .exchange()
                .expectStatus().isNoContent();

        verify(commentService, times(1)).deleteById(commentId);
    }
}