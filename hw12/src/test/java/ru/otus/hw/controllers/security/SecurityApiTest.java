package ru.otus.hw.controllers.security;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpMethod;
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
import ru.otus.hw.dto.AuthorDto;
import ru.otus.hw.dto.BookDto;
import ru.otus.hw.dto.CommentDto;
import ru.otus.hw.dto.GenreDto;
import ru.otus.hw.dto.api.BookCreateDto;
import ru.otus.hw.dto.api.BookUpdateDto;
import ru.otus.hw.formatters.ErrorMessageFormatter;
import ru.otus.hw.services.AuthorService;
import ru.otus.hw.services.BookService;
import ru.otus.hw.services.CommentService;
import ru.otus.hw.services.CustomUserDetailsService;
import ru.otus.hw.services.GenreService;

import java.util.List;

import java.util.Set;
import java.util.stream.Stream;


import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.request;
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
class SecurityApiTest {
    @Autowired
    private ObjectMapper mapper;

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


    static Stream<Endpoint> getApiEndpoints() {
        return Stream.of(
                new Endpoint(HttpMethod.GET, "/api/authors", "Get all authors"),
                new Endpoint(HttpMethod.GET, "/api/books", "Get all books"),
                new Endpoint(HttpMethod.GET, "/api/books/1", "Get book by id"),
                new Endpoint(HttpMethod.GET, "/api/books/1/comments", "Get comments for book")
        );
    }

    static Stream<Endpoint> deleteApiEndpoints() {
        return Stream.of(
                new Endpoint(HttpMethod.DELETE, "/api/books/1", "Delete book"),
                new Endpoint(HttpMethod.DELETE, "/api/books/1/comments/1", "Delete comment")
        );
    }

    static Stream<Endpoint> postApiEndpoints() {
        return Stream.of(
                new Endpoint(HttpMethod.POST, "/api/books/1/comments", "Add comment to book"),
                new Endpoint(HttpMethod.POST, "/api/books", "Create new book")
        );
    }

    static Stream<Endpoint> putApiEndpoints() {
        return Stream.of(
                new Endpoint(HttpMethod.PUT, "/api/books/1", "Update book")
        );
    }

    @ParameterizedTest
    @MethodSource({"getApiEndpoints", "deleteApiEndpoints", "postApiEndpoints", "putApiEndpoints"})
    @DisplayName("должен вернуть Redirect, если ЛЮБОЙ api endpoint с неавторизованным пользователем")
    void shouldRedirectApiAnyEndpointUnAuthorized(Endpoint endpoint) throws Exception {
        mockMvc.perform(request(endpoint.method, endpoint.path))
                .andExpect(status().is3xxRedirection());
    }

    @ParameterizedTest
    @MethodSource("getApiEndpoints")
    @DisplayName("должен вернуть OK, если GET api endpoint с авторизованным пользователем")
    @WithMockUser(username = "user")
    void shouldOkApiGetEndpointAuthorized(Endpoint endpoint) throws Exception {
        mockMvc.perform(request(endpoint.method, endpoint.path))
                .andExpect(status().isOk());
    }

    @ParameterizedTest
    @MethodSource("deleteApiEndpoints")
    @DisplayName("должен вернуть OK, если DELETE api endpoint с авторизованным пользователем")
    @WithMockUser(username = "user")
    void shouldOkApiDeleteEndpointAuthorized(Endpoint endpoint) throws Exception {
        mockMvc.perform(request(endpoint.method, endpoint.path))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("POST /api/books/{id}/comments – добавление нового комментария с авторизованным пользователем")
    @WithMockUser(username = "user")
    void shouldAddCommentToBookAuthorized() throws Exception {
        //given
        Long bookId = 5L;
        CommentDto newComment = new CommentDto(null, "Nice book", bookId);
        CommentDto savedComment = new CommentDto(99L, "Nice book", bookId);
        //when
        when(commentService.insert(newComment)).thenReturn(savedComment);
        //then
        mockMvc.perform(post("/api/books/{id}/comments", bookId)
                        .contentType(APPLICATION_JSON)
                        .content(mapper.writeValueAsString(newComment)))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "user")
    @DisplayName("POST /api/books - создаёт книгу с авторизованным пользователем")
    void shouldCreateBookAuthorized() throws Exception {
        //given
        BookCreateDto bookCreateDto = new BookCreateDto(
                0L, "Новая книга", 1L, Set.of(1L));
        BookDto newBookDto = new BookDto(
                99L,
                bookCreateDto.title(),
                new AuthorDto(bookCreateDto.authorId(), "Автор 1"),
                List.of(new GenreDto(1L, "Жанр 1"))
        );

        //when
        when(bookService.insert(bookCreateDto)).thenReturn(newBookDto);

        //then
        mockMvc.perform(post("/api/books")
                        .contentType(APPLICATION_JSON)
                        .content(mapper.writeValueAsString(bookCreateDto)))
                .andExpect(status().isOk());
    }


    @Test
    @WithMockUser(username = "user")
    @DisplayName("PUT /api/books/{id} - обновляет книгу с авторизованным пользователем")
    void shouldUpdateBookAuthorized() throws Exception {
        //given
        long id = 10L;
        BookUpdateDto updateDto = new BookUpdateDto(
                id,
                "Обновлённая книга",
                2L,
                Set.of(3L, 4L)
        );
        BookDto updated = new BookDto(
                id,
                updateDto.title(),
                new AuthorDto(updateDto.authorId(), "Автор 2"),
                List.of(
                        new GenreDto(3L, "Жанр 3"),
                        new GenreDto(4L, "Жанр 4")
                )
        );
        //when
        when(bookService.update(updateDto)).thenReturn(updated);

        //then
        mockMvc.perform(put("/api/books/{id}", id)
                        .contentType(APPLICATION_JSON)
                        .content(mapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk());
    }

    record Endpoint(HttpMethod method, String path, String description) {
    }
}