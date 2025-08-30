package ru.otus.hw.controllers.api;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import ru.otus.hw.controllers.handlers.GlobalExceptionHandler;
import ru.otus.hw.controllers.handlers.GlobalResponseEntityExceptionHandler;
import ru.otus.hw.controllers.handlers.ValidationExceptionHandler;
import ru.otus.hw.dto.CommentDto;
import ru.otus.hw.formatters.ErrorMessageFormatter;
import ru.otus.hw.services.CommentService;

import java.util.List;
import java.util.Locale;


import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Test class for the {@link CommentsController}
 */
@SuppressWarnings("unused")
@WebMvcTest(
        controllers = {CommentsController.class})
@AutoConfigureMockMvc(addFilters = false)
@Import({
        GlobalExceptionHandler.class,
        GlobalResponseEntityExceptionHandler.class,
        ValidationExceptionHandler.class
})
class CommentsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper mapper;

    @MockitoBean
    private CommentService commentService;

    @MockitoBean
    private ErrorMessageFormatter errorMessageFormatter;

    @MockitoBean
    private ValidationExceptionHandler validationExceptionHandler;

    @MockitoBean
    @Autowired
    private MessageSource messageSource;


    @Test
    @DisplayName("GET /api/books/{id}/comments – возвращает список комментариев")
    void shouldReturnCommentsForBook() throws Exception {
        //given
        Long bookId = 1L;
        List<CommentDto> expected = List.of(
                new CommentDto(1L, "First comment", bookId),
                new CommentDto(2L, "Second comment", bookId)
        );
        //when
        when(commentService.findByBookId(bookId)).thenReturn(expected);
        //then
        mockMvc.perform(get("/api/books/{id}/comments", bookId)
                        .accept(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(mapper.writeValueAsString(expected)))
                .andDo(print());
    }


    @Test
    @DisplayName("POST /api/books/{id}/comments – добавление нового комментария")
    void shouldAddCommentToBook() throws Exception {
        //given
        Long bookId = 5L;
        CommentDto newComment = new CommentDto(null, "Nice book", bookId);
        CommentDto savedComment = new CommentDto(99L, "Nice book", bookId);

        //when
        when(commentService.insert(newComment)).thenReturn(savedComment);

        when(messageSource.getMessage(
                eq("api.response.ok.save.comment"),
                any(Object[].class),
                any(Locale.class)))
                .thenReturn("Comment saved");

        //then
        mockMvc.perform(post("/api/books/{id}/comments", bookId)
                        .contentType(APPLICATION_JSON)
                        .content(mapper.writeValueAsString(newComment)))
                .andExpect(status().isCreated())

                .andExpect(jsonPath("$.id", is(savedComment.id().intValue())))
                .andExpect(jsonPath("$.text", is(savedComment.text())))
                .andExpect(jsonPath("$.bookId", is(savedComment.bookId().intValue())))
                .andDo(print());
    }


    @Test
    @DisplayName("DELETE /api/books/{id}/comments/{commentId} – удаление комментария")
    void ShouldDeleteCommentFromBook() throws Exception {
        //given
        Long bookId = 7L;
        long commentId = 123L;

        //when
        when(messageSource.getMessage(eq("api.response.ok.delete.comment"),
                any(Object[].class),
                any(Locale.class)))
                .thenReturn("Comment deleted");
        //then
        mockMvc.perform(delete("/api/books/{id}/comments/{commentId}",
                        bookId, commentId))
                .andExpect(status().isNoContent())
                .andDo(print());
        verify(commentService, times(1)).deleteById(commentId);
    }

}