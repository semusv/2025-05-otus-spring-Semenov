package ru.otus.hw.controllers;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.otus.hw.dto.AuthorDto;
import ru.otus.hw.dto.BookCreateDto;
import ru.otus.hw.dto.BookDto;
import ru.otus.hw.dto.BookUpdateDto;
import ru.otus.hw.dto.BookViewDto;
import ru.otus.hw.dto.CommentDto;
import ru.otus.hw.dto.GenreDto;
import ru.otus.hw.mappers.BookMapper;
import ru.otus.hw.services.AuthorService;
import ru.otus.hw.services.BookService;
import ru.otus.hw.services.CommentService;
import ru.otus.hw.services.GenreService;
import ru.otus.hw.exceptions.EntityNotFoundException;
import org.springframework.context.MessageSource;

import java.util.List;
import java.util.Locale;


import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(BookController.class)
class BookControllerTest {

    @Autowired
    private MockMvc mockMvc;

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
    private BookMapper bookMapper;

    @Test
    @DisplayName("GET /books - должен показать список всех книг")
    void getBooks_ShouldReturnBooksList() throws Exception {
        // given
        List<BookDto> expectedBooks = List.of(
                new BookDto(1L, "Книга 1", new AuthorDto(1L, "Автор 1"),
                        List.of(new GenreDto(1L, "Жанр 1"))),
                new BookDto(2L, "Книга 2", new AuthorDto(2L, "Автор 2"),
                        List.of(new GenreDto(2L, "Жанр 2")))
        );
        when(bookService.findAll()).thenReturn(expectedBooks);

        // when & then
        mockMvc.perform(get("/books"))
                .andExpect(status().isOk())
                .andExpect(view().name("books-list"))
                .andExpect(model().attributeExists("books"))
                .andExpect(model().attribute("books", expectedBooks));
    }

    @Test
    @DisplayName("GET /books/{id} - должен показать книгу с комментариями")
    void getBook_ShouldReturnBookView() throws Exception {
        // given
        long bookId = 1L;
        BookDto bookDto = new BookDto(bookId, "Книга",
                new AuthorDto(1L, "Автор"), List.of(new GenreDto(1L, "Жанр")));
        List<CommentDto> comments = List.of(
                new CommentDto(1L, "Комментарий 1", bookId),
                new CommentDto(2L, "Комментарий 2", bookId)
        );

        BookViewDto bookViewDto = new BookViewDto(
                bookId,
                "Книга",
                new AuthorDto(1L,
                        "Автор"),
                List.of(new GenreDto(1L, "Жанр")),
                List.of(
                        new CommentDto(1L, "Комментарий 1", bookId),
                        new CommentDto(2L, "Комментарий 2", bookId)
                ));

        when(bookService.findById(bookId)).thenReturn(bookDto);
        when(commentService.findByBookId(bookId)).thenReturn(comments);
        when(bookMapper.toBookViewDto(bookDto, comments)).thenReturn(bookViewDto);

        // when & then
        mockMvc.perform(get("/books/{id}", bookId))
                .andExpect(status().isOk())
                .andExpect(view().name("book-view"))
                .andExpect(model().attributeExists("book"))
                .andExpect(model().attribute("book", bookViewDto));
    }

    @Test
    @DisplayName("GET /books/{id}/edit - должен показать форму редактирования с данными книги")
    void editBook_ShouldReturnEditFormWithBookData() throws Exception {
        // given
        long bookId = 1L;
        BookDto bookDto = new BookDto(bookId, "Test Book", null, null);
        BookUpdateDto bookUpdateDto = new BookUpdateDto(bookId, "Test Book", 1L, List.of(1L, 2L));
        List<CommentDto> comments = List.of(new CommentDto(1L, "Комментарий", bookId));
        List<GenreDto> genres = List.of(new GenreDto(1L, "Жанр 1"), new GenreDto(2L, "Жанр 2"));
        List<AuthorDto> authors = List.of(new AuthorDto(1L, "Автор 1"), new AuthorDto(2L, "Автор 2"));

        when(bookService.findById(bookId)).thenReturn(bookDto);
        when(bookMapper.toBookUpdateDto(bookDto)).thenReturn(bookUpdateDto);
        when(commentService.findByBookId(bookId)).thenReturn(comments);
        when(genreService.findAll()).thenReturn(genres);
        when(authorService.findAll()).thenReturn(authors);

        // when & then
        mockMvc.perform(get("/books/{id}/edit", bookId))
                .andExpect(status().isOk())
                .andExpect(view().name("book-edit"))
                .andExpect(model().attributeExists("book"))
                .andExpect(model().attributeExists("comments"))
                .andExpect(model().attributeExists("allGenres"))
                .andExpect(model().attributeExists("allAuthors"))
                .andExpect(model().attribute("book", bookUpdateDto))
                .andExpect(model().attribute("allGenres", genres))
                .andExpect(model().attribute("allAuthors", authors))
                .andExpect(model().attribute("comments", comments));

        verify(bookService).findById(bookId);
        verify(bookMapper).toBookUpdateDto(bookDto);
        verify(commentService).findByBookId(bookId);
    }

    @Test
    @DisplayName("GET /books/{id}/edit - не должен перезаписывать book из flash-атрибутов")
    void editBook_WhenBookInFlashAttributes_ShouldNotOverwrite() throws Exception {
        // given
        Long bookId = 1L;
        BookUpdateDto existingBookDto = new BookUpdateDto(bookId, "Existing Book", 1L, List.of(1L));

        // when & then
        mockMvc.perform(get("/books/{id}/edit", bookId)
                        .flashAttr("book", existingBookDto))
                .andExpect(status().isOk())
                .andExpect(model().attribute("book", existingBookDto));

        verify(bookService, never()).findById(bookId);
        verify(bookMapper, never()).toBookUpdateDto(any());
    }


    @Test
    @DisplayName("POST /books/{id}/update - должен обновить книгу и остаться")
    void updateBook_ShouldUpdateAndRedirect() throws Exception {
        // given
        Long bookId = 1L;
        BookUpdateDto bookFormDto = new BookUpdateDto(
                bookId,
                "Новое название",
                1L,
                List.of(1L));

        // when & then
        mockMvc.perform(post("/books/{id}/update", bookId)
                        .flashAttr("book", bookFormDto))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/books/" + bookId + "/edit"));

        verify(bookService, times(1)).update(bookFormDto);
    }

    @Test
    @DisplayName("POST /books/{id}/delete - должен удалить книгу и перенаправить")
    void deleteBook_ShouldDeleteAndRedirect() throws Exception {
        // given
        long bookId = 1L;

        // when & then
        mockMvc.perform(post("/books/{id}/delete", bookId))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));

        verify(bookService, times(1)).deleteById(bookId);
    }

    @Test
    @DisplayName("GET /books/new - должен показать форму создания книги")
    void addBook_ShouldReturnCreateForm() throws Exception {
        // given
        List<GenreDto> genres = List.of(new GenreDto(1L, "Жанр"));
        List<AuthorDto> authors = List.of(new AuthorDto(1L, "Автор"));

        when(genreService.findAll()).thenReturn(genres);
        when(authorService.findAll()).thenReturn(authors);

        // when & then
        mockMvc.perform(get("/books/new"))
                .andExpect(status().isOk())
                .andExpect(view().name("book-edit"))
                .andExpect(model().attributeExists("book"))
                .andExpect(model().attributeExists("allGenres"))
                .andExpect(model().attributeExists("allAuthors"));
    }

    @Test
    @DisplayName("POST /books/create - должен создать книгу и перенаправить")
    void createBook_ShouldCreateAndRedirect() throws Exception {
        // given
        BookCreateDto bookFormDto = new BookCreateDto(0L, "Новая книга", 1L, List.of(1L));
        BookDto createdBook = new BookDto(1L, "Новая книга",
                new AuthorDto(1L, "Автор"), List.of(new GenreDto(1L, "Жанр")));

        when(bookService.insert(any(BookCreateDto.class))).thenReturn(createdBook);

        // when & then
        mockMvc.perform(post("/books/create")
                        .flashAttr("book", bookFormDto))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/books/1"));

        verify(bookService, times(1)).insert(any(BookCreateDto.class));
    }

    @Test
    @DisplayName("GET /books/{id} с несуществующим ID - должен вернуть страницу ошибки и код 404")
    void getBook_WhenNotExists_ShouldReturnErrorPage() throws Exception {
        // given
        long nonExistentId = 999L;
        when(bookService.findById(nonExistentId)).thenThrow(new EntityNotFoundException(""));

        String expectedErrorText = "Entity not found";
        when(messageSource.getMessage(
                eq("entity-not-found-error"),
                any(),
                any(Locale.class))
        ).thenReturn(expectedErrorText);

        // when & then
        mockMvc.perform(get("/books/{id}", nonExistentId))
                .andExpect(status().isNotFound())
                .andExpect(view().name("customError"))
                .andExpect(model().attributeExists("errorText"))
                .andExpect(model().attribute("errorText", expectedErrorText));
    }
}