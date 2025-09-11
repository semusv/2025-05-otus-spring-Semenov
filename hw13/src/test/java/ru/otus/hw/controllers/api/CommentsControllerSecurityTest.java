package ru.otus.hw.controllers.api;

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
import ru.otus.hw.controllers.handlers.CustomAccessDeniedHandler;
import ru.otus.hw.controllers.handlers.GlobalExceptionHandler;
import ru.otus.hw.controllers.handlers.GlobalResponseEntityExceptionHandler;
import ru.otus.hw.controllers.handlers.ValidationExceptionHandler;
import ru.otus.hw.dto.CommentDto;
import ru.otus.hw.formatters.ErrorMessageFormatter;
import ru.otus.hw.services.BookService;
import ru.otus.hw.services.CommentService;
import ru.otus.hw.services.CustomUserDetailsService;
import ru.otus.hw.services.ErrorHandlingService;
import java.util.stream.Stream;

import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {
        BooksController.class,
        CommentsController.class,
        CustomAccessDeniedHandler.class
})
@Import({
        GlobalExceptionHandler.class,
        GlobalResponseEntityExceptionHandler.class,
        ValidationExceptionHandler.class,
        SecurityConfig.class
})
class CommentsControllerSecurityTest {
    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BookService bookService;

    @MockitoBean
    private CommentService commentService;

    @MockitoBean
    private CustomUserDetailsService userDetailsService;

    @MockitoBean
    ErrorMessageFormatter errorMessageFormatter;

    @MockitoBean
    private ValidationExceptionHandler validationExceptionHandler;

    @MockitoBean
    ErrorHandlingService errorHandlingService;

    static Stream<Endpoint> getApiEndpoints() {
        return Stream.of(
                new Endpoint(HttpMethod.GET, "/api/books/1/comments", "Get comments for book")
        );
    }

    static Stream<Endpoint> deleteApiEndpoints() {
        return Stream.of(
                new Endpoint(HttpMethod.DELETE, "/api/books/1/comments/1", "Delete comment")
        );
    }

    static Stream<Endpoint> postApiEndpoints() {
        return Stream.of(
                new Endpoint(HttpMethod.POST, "/api/books/1/comments", "Add comment to book")
        );
    }

    static Stream<Endpoint> putApiEndpoints() {
        return Stream.of(
                new Endpoint(HttpMethod.POST, "/api/books/1/comments", "Add comment to book")
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
                .andExpect(status().isNoContent());
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
                .andExpect(status().isCreated());
    }




    record Endpoint(HttpMethod method, String path, String description) {
    }
}