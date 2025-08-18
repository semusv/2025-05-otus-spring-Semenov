package ru.otus.hw.controllers.pages;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import static org.junit.jupiter.api.Assertions.assertTrue;
/**
 * Test class for the {@link GenresPagesController}
 */
@WebFluxTest(
        controllers = GenresPagesController.class,
        excludeFilters = {
                @ComponentScan.Filter(
                        type = FilterType.ANNOTATION,
                        classes = ControllerAdvice.class),
                @ComponentScan.Filter(
                        type = FilterType.ANNOTATION,
                        classes = RestControllerAdvice.class),
        }
)
class GenrePagesControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @Test
    @DisplayName("Получение списка жанров должно возвращать страницу с жанрами")
    void getAll_ShouldReturnGenresListPage(){

        // when & then
        webTestClient.get()
                .uri("/genres")
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .value(body -> assertTrue(body.contains("genres-list")));

    }
}