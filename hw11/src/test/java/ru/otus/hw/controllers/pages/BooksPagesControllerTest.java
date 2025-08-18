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
 * Test class for the {@link BooksPagesController}
 */
@WebFluxTest(
        controllers = BooksPagesController.class,
        excludeFilters = {
                @ComponentScan.Filter(
                        type = FilterType.ANNOTATION,
                        classes = ControllerAdvice.class),
                @ComponentScan.Filter(
                        type = FilterType.ANNOTATION,
                        classes = RestControllerAdvice.class),
        }
)
class BooksPagesControllerTest {

    @Autowired
    private WebTestClient webTestClient;


    @Test
    @DisplayName("GET /books - должен показать страницу списка всех книг")
    void getBooks_ShouldReturnBooksList() {

        // when & then
        webTestClient.get()
                .uri("/books")
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .value(body -> assertTrue(body.contains("books-list")));

    }

    @Test
    @DisplayName("GET /books/{id} - должен показать страницу книги с комментариями")
    void getBook_ShouldReturnBookView() {
        // given
        long bookId = 1L;

        // when & then
        webTestClient.get()
                .uri("/books/{id}", bookId)
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .value(body -> assertTrue(body.contains("book-view")));
    }

    @Test
    @DisplayName("GET /books/{id}/edit - должен показать форму редактирования с данными книги")
    void editBook_ShouldReturnEditFormWithBookData() {
        // given
        long bookId = 1L;

        // when & then
        webTestClient.get()
                .uri("/books/{id}/edit", bookId)
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .value(body -> assertTrue(body.contains("book-edit")));

    }

    @Test
    @DisplayName("GET /books/new - должен показать форму создания книги")
    void addBook_ShouldReturnCreateForm() {

        // when & then
        webTestClient.get()
                .uri("/books/new")
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .value(body -> assertTrue(body.contains("book-edit")));
    }

}