package ru.otus.hw.controllers.api;

import com.fasterxml.jackson.core.JsonProcessingException;
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
import ru.otus.hw.dto.GenreDto;
import ru.otus.hw.formatters.ErrorMessageFormatter;
import ru.otus.hw.services.GenreService;
import java.util.List;
import static org.mockito.Mockito.when;

/**
 * Тесты для {@link GenresController}
 */
@SuppressWarnings("unused")
@WebFluxTest(
        controllers = GenresController.class
)
@Import({
        GlobalExceptionHandler.class,
        GlobalResponseEntityExceptionHandler.class,
        ValidationExceptionHandler.class
})
class GenresControllerTest {
    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private ObjectMapper mapper;

    @MockitoBean
    private GenreService genreService;

    @MockitoBean
    private ErrorMessageFormatter errorMessageFormatter;


    @Test
    @DisplayName("должен вывести полный список жанров")
    void shouldReturnGenreList() throws JsonProcessingException {
        //given
        List<GenreDto> expectedGenres = List.of(
                new GenreDto(1L, "Роман"),
                new GenreDto(2L, "Фантастика")
        );
        //when
        when(genreService.findAll())
                .thenReturn(Flux.fromIterable(expectedGenres));
        //then
        webTestClient.get()
                .uri("/api/genres")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType("application/json")
                .expectBody()
                .json(mapper.writeValueAsString(expectedGenres));
    }
}