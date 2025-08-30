package ru.otus.hw.controllers.security;


import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.otus.hw.config.SecurityConfig;
import ru.otus.hw.controllers.api.AuthorsController;
import ru.otus.hw.controllers.api.BooksController;
import ru.otus.hw.controllers.api.CommentsController;
import ru.otus.hw.controllers.api.GenresController;
import ru.otus.hw.controllers.handlers.GlobalExceptionHandler;
import ru.otus.hw.controllers.handlers.GlobalResponseEntityExceptionHandler;
import ru.otus.hw.controllers.handlers.ValidationExceptionHandler;
import ru.otus.hw.controllers.pages.AuthorsPagesController;
import ru.otus.hw.controllers.pages.BooksPagesController;
import ru.otus.hw.controllers.pages.GenresPagesController;
import ru.otus.hw.formatters.ErrorMessageFormatter;
import ru.otus.hw.services.AuthorService;
import ru.otus.hw.services.BookService;
import ru.otus.hw.services.CommentService;
import ru.otus.hw.services.CustomUserDetailsService;
import ru.otus.hw.services.GenreService;

import java.util.stream.Stream;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SuppressWarnings("unused")
@WebMvcTest(controllers = {
        AuthorsController.class,
        BooksController.class,
        CommentsController.class,
        GenresController.class,
        AuthorsPagesController.class,
        BooksPagesController.class,
        GenresPagesController.class
})
@Import({
        GlobalExceptionHandler.class,
        GlobalResponseEntityExceptionHandler.class,
        ValidationExceptionHandler.class,
        SecurityConfig.class
})
class SecurityPagesTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthorService authorService;

    @MockitoBean
    private BookService bookService;

    @MockitoBean
    private CommentService commentService;

    @MockitoBean
    private GenreService genreService;

    @MockitoBean
    private CustomUserDetailsService userDetailsService;

    @MockitoBean
    ErrorMessageFormatter errorMessageFormatter;

    @MockitoBean
    private ValidationExceptionHandler validationExceptionHandler;

    static Stream<String> protectedPagesEndpoints() {
        return Stream.of(
                "/authors",
                "/",
                "/books",
                "/books/1",
                "/books/1/edit",
                "/books/new",
                "/genres"
        );
    }

    static Stream<String> publicEndpoints() {
        return Stream.of(
                "/login"
        );
    }

    @ParameterizedTest
    @MethodSource("protectedPagesEndpoints")
    @DisplayName("должен вернуть Redirect, если page endpoint с неавторизованным пользователем")
    void shouldRedirectPagesEndpointUnAuthorized(String endpoint) throws Exception {
        mockMvc.perform(get(endpoint)).andExpect(status().is3xxRedirection());
    }

    @ParameterizedTest
    @MethodSource("protectedPagesEndpoints")
    @DisplayName("должен вернуть OK, если page endpoint с авторизованным пользователем")
    @WithMockUser(username = "user")
    void shouldOkPagesEndpointUnAuthorized(String endpoint) throws Exception {
        mockMvc.perform(get(endpoint)).andExpect(status().isOk());
    }

    @ParameterizedTest
    @MethodSource("publicEndpoints")
    void whenUnauthenticated_thenPublicEndpointsShouldBeAccessible(String endpoint) throws Exception {
        mockMvc.perform(get(endpoint)).andExpect(status().isOk());
    }

}