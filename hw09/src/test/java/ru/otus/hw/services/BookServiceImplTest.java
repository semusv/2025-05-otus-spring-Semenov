package ru.otus.hw.services;

import jakarta.validation.ValidationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import ru.otus.hw.dto.BookFormDto;
import ru.otus.hw.mappers.BookMapperImpl;
import ru.otus.hw.dto.BookDto;
import ru.otus.hw.exceptions.EntityNotFoundException;
import ru.otus.hw.services.validators.BookValidatorImpl;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

@DisplayName("Сервис для работы с книгами")
@DataJpaTest
@Import({
        BookServiceImpl.class,
        BookMapperImpl.class,
        BookValidatorImpl.class
})
class BookServiceImplTest {

    @Autowired
    private BookService bookService;

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
        List<Long> genresIds = List.of(1L, 2L);

        BookFormDto bookFormDto = BookFormDto.builder()
                .id(bookId)
                .title(updatedTitle)
                .authorId(authorId)
                .genreIds(genresIds).build();

        BookDto result = bookService.update(bookFormDto);

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
        List<Long> genresIds = List.of(1L, 2L);

        BookFormDto bookFormDto = BookFormDto.builder()
                .title(title)
                .authorId(authorId)
                .genreIds(genresIds).build();

        BookDto result = bookService.insert(bookFormDto);

        assertThat(result.id()).isPositive();
        assertThat(result.title()).isEqualTo(title);
        assertThat(result.author().id()).isEqualTo(authorId);
        assertThat(result.genres()).hasSize(2);
    }

    @Test
    @DisplayName("должен выбрасывать исключение при создании книги с пустым названием")
    void shouldThrowWhenCreateBookWithEmptyTitle() {
        BookFormDto bookFormDto = BookFormDto.builder()
                .title("")
                .authorId(1L)
                .genreIds(List.of(1L)).build();

        assertThatThrownBy(() -> bookService.insert(bookFormDto))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Title cannot be empty");
    }

    @Test
    @DisplayName("должен выбрасывать исключение при создании книги без жанров")
    void shouldThrowWhenCreateBookWithoutGenres() {
        BookFormDto bookFormDto = BookFormDto.builder()
                .title("Title")
                .authorId(1L)
                .genreIds(List.of()).build();

        assertThatThrownBy(() -> bookService.insert(bookFormDto))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Genres cannot be empty");
    }

    @Test
    @DisplayName("должен выбрасывать исключение при создании книги с несуществующим автором")
    void shouldThrowWhenCreateBookWithNonExistingAuthor() {
        BookFormDto bookFormDto = BookFormDto.builder()
                .title("Title")
                .authorId(999L)
                .genreIds(List.of(1L)).build();

        assertThatThrownBy(() -> bookService.insert(bookFormDto))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Author with id 999 not found");
    }

    @Test
    @DisplayName("должен выбрасывать исключение при создании книги с несуществующими жанрами")
    void shouldThrowWhenCreateBookWithNonExistingGenres() {
        BookFormDto bookFormDto = BookFormDto.builder()
                .title("Title")
                .authorId(1L)
                .genreIds(List.of(999L)).build();

        assertThatThrownBy(() -> bookService.insert(bookFormDto))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Genres not found: [999]");
    }

    @Test
    @DisplayName("должен выбрасывать исключение при обновлении книги с пустым названием")
    void shouldThrowWhenUpdateBookWithEmptyTitle() {
        BookFormDto bookFormDto = BookFormDto.builder()
                .id(1L)
                .title("")
                .authorId(1L)
                .genreIds(List.of(1L)).build();

        assertThatThrownBy(() -> bookService.update(bookFormDto))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Title cannot be empty");
    }

    @Test
    @DisplayName("должен выбрасывать исключение при обновлении книги без жанров")
    void shouldThrowWhenUpdateBookWithoutGenres() {
        BookFormDto bookFormDto = BookFormDto.builder()
                .id(1L)
                .title("Title")
                .authorId(1L)
                .genreIds(List.of()).build();

        assertThatThrownBy(() -> bookService.update(bookFormDto))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Genres cannot be empty");
    }

    @Test
    @DisplayName("должен выбрасывать исключение при обновлении книги с несуществующим автором")
    void shouldThrowWhenUpdateBookWithNonExistingAuthor() {
        BookFormDto bookFormDto = BookFormDto.builder()
                .id(1L)
                .title("Title")
                .authorId(999L)
                .genreIds(List.of(1L)).build();

        assertThatThrownBy(() -> bookService.update(bookFormDto))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Author with id 999 not found");
    }

    @Test
    @DisplayName("должен выбрасывать исключение при обновлении книги с несуществующими жанрами")
    void shouldThrowWhenUpdateBookWithNonExistingGenres() {
        BookFormDto bookFormDto = BookFormDto.builder()
                .id(1L)
                .title("Title")
                .authorId(1L)
                .genreIds(List.of(999L)).build();

        assertThatThrownBy(() -> bookService.update(bookFormDto))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Genres not found: [999]");
    }
}