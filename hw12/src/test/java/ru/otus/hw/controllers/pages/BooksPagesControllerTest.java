package ru.otus.hw.controllers.pages;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

/**
 * Test class for the {@link BooksPagesController}
 */
@WebMvcTest(
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
@AutoConfigureMockMvc(addFilters = false)
class BooksPagesControllerTest {

    @Autowired
    private MockMvc mockMvc;


    @Test
    @DisplayName("GET /books - должен показать страницу списка всех книг")
    void getBooks_ShouldReturnBooksList() throws Exception {

        // when & then
        mockMvc.perform(get("/books"))
                .andExpect(status().isOk())
                .andExpect(view().name("books-list"));
    }

    @Test
    @DisplayName("GET /books/{id} - должен показать страницу книги с комментариями")
    void getBook_ShouldReturnBookView() throws Exception {
        // given
        long bookId = 1L;

        // when & then
        mockMvc.perform(get("/books/{id}", bookId))
                .andExpect(status().isOk())
                .andExpect(view().name("book-view"));
    }

    @Test
    @DisplayName("GET /books/{id}/edit - должен показать форму редактирования с данными книги")
    void editBook_ShouldReturnEditFormWithBookData() throws Exception {
        // given
        long bookId = 1L;

        // when & then
        mockMvc.perform(get("/books/{id}/edit", bookId))
                .andExpect(status().isOk())
                .andExpect(view().name("book-edit"));
    }

    @Test
    @DisplayName("GET /books/new - должен показать форму создания книги")
    void addBook_ShouldReturnCreateForm() throws Exception {

        // when & then
        mockMvc.perform(get("/books/new"))
                .andExpect(status().isOk())
                .andExpect(view().name("book-edit"));
    }

}