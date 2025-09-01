package ru.otus.hw.controllers.api;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;


import static org.hamcrest.Matchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.springframework.context.MessageSource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import ru.otus.hw.controllers.handlers.GlobalExceptionHandler;
import ru.otus.hw.controllers.handlers.GlobalResponseEntityExceptionHandler;
import ru.otus.hw.controllers.handlers.ValidationExceptionHandler;
import ru.otus.hw.dto.BookDto;
import ru.otus.hw.exceptions.EntityNotFoundException;
import ru.otus.hw.formatters.ErrorMessageFormatter;
import ru.otus.hw.services.AuthorService;
import ru.otus.hw.services.BookService;
import ru.otus.hw.dto.api.BookCreateDto;
import ru.otus.hw.dto.api.BookUpdateDto;

import java.util.List;
import java.util.Locale;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

import ru.otus.hw.dto.AuthorDto;
import ru.otus.hw.dto.GenreDto;
import ru.otus.hw.services.CommentService;
import ru.otus.hw.services.ErrorHandlingService;
import ru.otus.hw.services.ErrorHandlingServiceImpl;
import ru.otus.hw.services.GenreService;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SuppressWarnings("unused")
@WebMvcTest(
        controllers = {
                BooksController.class
        })
@Import({
        GlobalExceptionHandler.class,
        GlobalResponseEntityExceptionHandler.class,
        ValidationExceptionHandler.class,
        ErrorHandlingServiceImpl.class
})
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("Контроллер API книг")
class BooksControllerTest {

    private static final String API_URL = "/api/books";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper mapper;

    @MockitoBean
    @Autowired
    private BookService bookService;

    @MockitoBean
    @Autowired
    private CommentService commentService;

    @MockitoBean
    @Autowired
    private GenreService genreService;

    @MockitoBean
    @Autowired
    private AuthorService authorService;

    @MockitoBean
    @Autowired
    private MessageSource messageSource;

    @MockitoBean
    @Autowired
    private ErrorMessageFormatter errorMessageFormatter;



    @Test
    @DisplayName("GET /api/books - возвращает список всех книг")
    void shouldReturnBooksList() throws Exception {
        //given
        List<BookDto> expectedBooks = List.of(
                new BookDto(1L, "Книга 1",
                        new AuthorDto(1L, "Автор 1"),
                        List.of(new GenreDto(1L, "Жанр 1"))),
                new BookDto(2L, "Книга 2",
                        new AuthorDto(2L, "Автор 2"),
                        List.of(new GenreDto(2L, "Жанр 2")))
        );
        //when
        when(bookService.findAll()).thenReturn(expectedBooks);

        //then
        mockMvc.perform(get(API_URL))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().json(mapper.writeValueAsString(expectedBooks)));
    }

    @Test
    @DisplayName("GET /api/books/{id} - возвращает развёрнутый объект книги")
    void shouldReturnBook() throws Exception {
        //given
        long bookId = 1L;
        BookDto bookDto = new BookDto(
                bookId,
                "Книга",
                new AuthorDto(1L, "Автор"),
                List.of(new GenreDto(1L, "Жанр")));

        //when
        when(bookService.findById(bookId)).thenReturn(bookDto);

        //then
        mockMvc.perform(get("/api/books/{id}", bookId)
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(mapper.writeValueAsString(bookDto)))
                .andDo(print());
    }

    @Test
    @DisplayName("POST /api/books - создаёт книгу и возвращает ResponseDto")
    void shouldCreateAndReturnResponse() throws Exception {
        //given
        BookCreateDto formDto = new BookCreateDto(
                0L, "Новая книга", 1L, Set.of(1L));
        BookDto created = new BookDto(
                99L,
                formDto.title(),
                new AuthorDto(formDto.authorId(), "Автор 1"),
                List.of(new GenreDto(1L, "Жанр 1"))
        );

        //when
        when(bookService.insert(formDto)).thenReturn(created);
        when(messageSource.getMessage(eq("api.response.ok.save.book"),
                any(Object[].class), any(Locale.class)))
                .thenReturn("Book saved");

        //then
        mockMvc.perform(post(API_URL)
                        .contentType(APPLICATION_JSON)
                        .content(mapper.writeValueAsString(formDto)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", Matchers.is(created.id().intValue())))
                .andExpect(jsonPath("$.title", Matchers.is(created.title())))
                .andExpect(jsonPath("$.author.id", Matchers.is(created.author().id().intValue())))
                .andExpect(jsonPath("$.genres[0].id", Matchers.is(created.genres().get(0).id().intValue())));

        verify(bookService, times(1)).insert(formDto);
    }


    @Test
    @DisplayName("PUT /api/books/{id} - обновляет книгу и возвращает ResponseDto")
    void shouldUpdateAndReturnResponse() throws Exception {
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
        when(messageSource.getMessage(eq("api.response.ok.save.book"),
                any(Object[].class), any(Locale.class)))
                .thenReturn("Book updated");

        //then
        mockMvc.perform(put("/api/books/{id}", id)
                        .contentType(APPLICATION_JSON)
                        .content(mapper.writeValueAsString(updateDto)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", Matchers.is(updated.id().intValue())))
                .andExpect(jsonPath("$.title", Matchers.is(updated.title())))
                .andExpect(jsonPath("$.author.id", Matchers.is(updated.author().id().intValue())))
                .andExpect(jsonPath("$.genres", hasSize(updated.genres().size())));

        verify(bookService, times(1)).update(updateDto);
    }


    @Test
    @DisplayName("DELETE /api/books/{id} - удаляет книгу и возвращает ResponseDto")
    void shouldDeleteAndReturnResponse() throws Exception {
        //given
        long id = 55L;

        //when
        when(messageSource.getMessage(
                eq("api.response.ok.delete.book"),
                any(Object[].class),
                any(Locale.class)))
                .thenReturn("Book deleted");

        //then
        mockMvc.perform(delete("/api/books/{id}", id))
                .andDo(print())
                .andExpect(status().isNoContent());

        verify(bookService, times(1)).deleteById(id);
    }


    @Test
    @DisplayName("POST /api/books - возвращает 400, когда пустые поля")
    void shouldReturnResponse400WhenEmptyFields() throws Exception {
        //given
        BookCreateDto formDto = new BookCreateDto(
                0L, "Новая книга", null, Set.of(1L));
        //then
        mockMvc.perform(post(API_URL)
                        .contentType(APPLICATION_JSON)
                        .content(mapper.writeValueAsString(formDto)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }


    @Test
    @DisplayName("GET /api/books/{id} - возвращает 404, когда некорректный ID книги")
    void shouldReturnResponse404WhenWrongBookId() throws Exception {
        //given
        long bookId = 999L;

        //when
        when(bookService.findById(bookId))
                .thenThrow(new EntityNotFoundException(
                        "Book with id %d not found".formatted(bookId),
                        "exception.entity.not.found.book",
                        bookId));

        when(messageSource.getMessage(
                any(String.class),
                any(Object[].class),
                any(String.class),
                any(Locale.class)))
                .thenReturn("Not Found Book with ID");

        //then
        mockMvc.perform(get("/api/books/{id}", bookId)
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andDo(print());
    }

    @Test
    @DisplayName("POST /api/books - возвращает 500, когда некорректный путь")
    void shouldReturnResponse500WhenWrongUrl() throws Exception {
        //given
        BookCreateDto formDto = new BookCreateDto(
                0L, "Новая книга", 1L, Set.of(1L));
        //then
        mockMvc.perform(post("/api/wrongUrl")
                        .contentType(APPLICATION_JSON)
                        .content(mapper.writeValueAsString(formDto)))
                .andExpect(status().isInternalServerError())
                .andDo(print());
    }

}
