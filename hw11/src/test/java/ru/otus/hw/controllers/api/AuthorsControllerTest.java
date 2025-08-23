package ru.otus.hw.controllers.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import ru.otus.hw.controllers.handlers.GlobalExceptionHandler;
import ru.otus.hw.controllers.handlers.GlobalResponseEntityExceptionHandler;
import ru.otus.hw.controllers.handlers.ValidationExceptionHandler;
import ru.otus.hw.dto.AuthorDto;
import ru.otus.hw.formatters.ErrorMessageFormatter;
import ru.otus.hw.services.AuthorService;

import java.util.List;

import static org.mockito.Mockito.when;

@SuppressWarnings("unused")
@WebFluxTest(controllers = AuthorsController.class)
@Import({
        GlobalExceptionHandler.class,
        GlobalResponseEntityExceptionHandler.class,
        ValidationExceptionHandler.class
})
class AuthorsControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private ObjectMapper mapper;

    @MockitoBean
    private AuthorService authorService;

    @MockitoBean
    private ErrorMessageFormatter errorMessageFormatter;

    @Test
    @DisplayName("должен вывести полный список авторов")
    void shouldReturnAuthorList() {
        //given
        List<AuthorDto> expectedAuthors = List.of(
                new AuthorDto(1L, "Лев Толстой"),
                new AuthorDto(2L, "Фёдор Достоевский")
        );

        //when
        when(authorService.findAll()).thenReturn(Flux.fromIterable(expectedAuthors));

        //then
        webTestClient.get()
                .uri("/api/authors")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(AuthorDto.class)
                .isEqualTo(expectedAuthors);
    }
}