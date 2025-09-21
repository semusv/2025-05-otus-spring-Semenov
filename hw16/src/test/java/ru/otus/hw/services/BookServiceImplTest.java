package ru.otus.hw.services;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import ru.otus.hw.dto.BookDto;
import ru.otus.hw.dto.api.BookFormDto;
import ru.otus.hw.exceptions.EntityNotFoundException;
import ru.otus.hw.mappers.AuthorMapperImpl;
import ru.otus.hw.mappers.BookMapperImpl;
import ru.otus.hw.mappers.CommentMapperImpl;
import ru.otus.hw.mappers.GenreMapperImpl;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

@DisplayName("Сервис для работы с книгами")
@DataJpaTest
@Import({
        BookServiceImpl.class,
        BookMapperImpl.class,
        AuthorMapperImpl.class,
        GenreMapperImpl.class,
        CommentMapperImpl.class
})
class BookServiceImplTest {

    @Autowired
    private BookService bookService;

    @Autowired
    @MockitoBean
    private AclServiceWrapperService aclServiceWrapperService;


    @Test
    @DisplayName("должен загружать книгу по id с автором и жанрами")
    void shouldFindBookByIdWithAuthorAndGenres() {
        BookDto book = bookService.findById(1L);

        assertThat(book).isNotNull();
        assertThat(book.id()).isPositive();
        assertThat(book.title()).isNotBlank();
        assertThat(book.author()).isNotNull();
        assertThat(book.author().fullName()).isNotBlank();
        assertThat(book.genres()).isNotEmpty();
    }

    @Test
    @DisplayName("должен выбрасывать исключение при загрузке несуществующей книги")
    void shouldThrowWhenFindNonExistingBook() {
        assertThatThrownBy(() -> bookService.findById(999L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Book with id 999 not found");
    }

    @Test
    @DisplayName("должен загружать список всех книг с авторами и жанрами")
    void shouldFindAllBooksWithAuthorsAndGenres() {
        List<BookDto> result = bookService.findAll();

        assertThat(result).isNotEmpty();
        result.forEach(book -> {
            assertThat(book.id()).isPositive();
            assertThat(book.title()).isNotBlank();
            assertThat(book.author()).isNotNull();
            assertThat(book.author().fullName()).isNotBlank();
            assertThat(book.genres()).isNotEmpty();
        });
    }

    @Test
    @DisplayName("должен удалять книгу по id")
    void shouldDeleteBookById() {
        long bookId = 1L;
        assertThat(bookService.findById(bookId)).isNotNull();

        bookService.deleteById(bookId);

        assertThatThrownBy(() -> bookService.findById(bookId))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Book with id 1 not found");
    }

    @Test
    @DisplayName("должен обновлять данные книги")
    void shouldUpdateBook() {
        long bookId = 1L;
        String updatedTitle = "Updated Title";
        long authorId = 1L;
        Set<Long> genresIds = Set.of(1L, 2L);

        BookFormDto bookFormDto = BookFormDto.builder()
                .title(updatedTitle)
                .authorId(authorId)
                .genreIds(genresIds).build();

        BookDto result = bookService.update(bookId, bookFormDto);

        assertThat(result.id()).isEqualTo(bookId);
        assertThat(result.title()).isEqualTo(updatedTitle);
        assertThat(result.author().id()).isEqualTo(authorId);
        assertThat(result.genres()).hasSize(2);
    }

    @Test
    @DisplayName("должен создавать новую книгу")
    void shouldCreateNewBook() {
        String title = "New Book Title";
        long authorId = 1L;
        Set<Long> genresIds = Set.of(1L, 2L);

        BookFormDto bookCreateDto = BookFormDto.builder()
                .title(title)
                .authorId(authorId)
                .genreIds(genresIds).build();

        BookDto result = bookService.insert(bookCreateDto);

        assertThat(result.id()).isPositive();
        assertThat(result.title()).isEqualTo(title);
        assertThat(result.author().id()).isEqualTo(authorId);
        assertThat(result.genres()).hasSize(2);
    }


    @Test
    @DisplayName("должен выбрасывать исключение при создании книги с несуществующим автором")
    void shouldThrowWhenCreateBookWithNonExistingAuthor() {
        BookFormDto bookCreateDto = BookFormDto.builder()
                .title("Title")
                .authorId(999L)
                .genreIds(Set.of(1L)).build();

        assertThatThrownBy(() -> bookService.insert(bookCreateDto))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Author with id 999 not found");
    }

    @Test
    @DisplayName("должен выбрасывать исключение при создании книги с несуществующими жанрами")
    void shouldThrowWhenCreateBookWithNonExistingGenres() {
        BookFormDto bookCreateDto = BookFormDto.builder()
                .title("Title")
                .authorId(1L)
                .genreIds(Set.of(999L)).build();

        assertThatThrownBy(() -> bookService.insert(bookCreateDto))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Genres not found: [999]");
    }


    @Test
    @DisplayName("должен выбрасывать исключение при обновлении книги с несуществующим автором")
    void shouldThrowWhenUpdateBookWithNonExistingAuthor() {
        long bookId = 1L;
        BookFormDto bookFormDto = BookFormDto.builder()
                .title("Title")
                .authorId(999L)
                .genreIds(Set.of(1L)).build();

        assertThatThrownBy(() -> bookService.update(bookId, bookFormDto))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Author with id 999 not found");
    }

    @Test
    @DisplayName("должен выбрасывать исключение при обновлении книги с несуществующими жанрами")
    void shouldThrowWhenUpdateBookWithNonExistingGenres() {
        long bookId = 1L;
        BookFormDto bookFormDto = BookFormDto.builder()
                .title("Title")
                .authorId(1L)
                .genreIds(Set.of(999L)).build();

        assertThatThrownBy(() -> bookService.update(bookId, bookFormDto))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Genres not found: [999]");
    }
}