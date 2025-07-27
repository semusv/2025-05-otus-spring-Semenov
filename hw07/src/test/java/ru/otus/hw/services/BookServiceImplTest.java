package ru.otus.hw.services;

import jakarta.validation.ValidationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import ru.otus.hw.mappers.AuthorMapper;
import ru.otus.hw.mappers.BookMapper;
import ru.otus.hw.mappers.CommentMapper;
import ru.otus.hw.mappers.GenreMapper;
import ru.otus.hw.dto.BookDto;
import ru.otus.hw.dto.GenreDto;
import ru.otus.hw.exceptions.EntityNotFoundException;
import ru.otus.hw.services.validators.BookValidatorImpl;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

@DisplayName("Интеграционный тест для сервиса книг")
@DataJpaTest
@Import({
        BookServiceImpl.class,
        BookMapper.class,
        AuthorMapper.class,
        GenreMapper.class,
        CommentMapper.class,
        BookValidatorImpl.class
})
class BookServiceImplTest {

    private final BookService bookService;


    @Autowired
    BookServiceImplTest(BookService bookService) {
        this.bookService = bookService;
    }

    @Test
    @DisplayName("Должен находить книгу по id с полной информацией")
    void findById_shouldFindBookWithFullInfo() {
        Optional<BookDto> result = bookService.findById(1L);

        assertThat(result).isPresent();
        BookDto book = result.get();

        assertThat(book.id()).isPositive();
        assertThat(book.title()).isNotBlank();

        assertThat(book.author()).isNotNull();
        assertThat(book.author().fullName()).isNotBlank();

        assertThat(book.genres()).isNotEmpty();
        book.genres().forEach(genre -> {
            assertThat(genre.id()).isPositive();
            assertThat(genre.name()).isNotBlank();
        });
    }

    @Test
    @DisplayName("Должен возвращать пустой Optional при поиске несуществующей книги")
    void findById_shouldReturnEmptyForNonExistingId() {
        Optional<BookDto> result = bookService.findById(999L);
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Должен возвращать список всех книг с полной информацией")
    void findAll_shouldReturnAllBooksWithFullInfo() {
        List<BookDto> result = bookService.findAll();

        assertThat(result).isNotEmpty();
        result.forEach(book -> {
            assertThat(book.id()).isPositive();
            assertThat(book.title()).isNotBlank();

            assertThat(book.author()).isNotNull();
            assertThat(book.author().fullName()).isNotBlank();

            assertThat(book.genres()).isNotEmpty();
            book.genres().forEach(genre -> {
                assertThat(genre.id()).isPositive();
                assertThat(genre.name()).isNotBlank();
            });
        });
    }

    @Test
    @DisplayName("Должен удалять книгу по id")
    void deleteById_shouldDeleteBook() {
        long bookId = 1L;
        assertThat(bookService.findById(bookId)).isPresent();

        bookService.deleteById(bookId);

        assertThat(bookService.findById(bookId)).isEmpty();
    }


    @Test
    @DisplayName("Должен обновлять существующую книгу")
    void update_shouldUpdateExistingBook() {
        long bookId = 1L;
        String updatedTitle = "Updated Title";
        long authorId = 1L;
        Set<Long> genresIds = Set.of(1L, 2L);

        BookDto result = bookService.update(bookId, updatedTitle, authorId, genresIds);

        assertThat(result.id()).isEqualTo(bookId);
        assertThat(result.title()).isEqualTo(updatedTitle);
        assertThat(result.author().id()).isEqualTo(authorId);
        assertThat(result.genres()).hasSize(2)
                .extracting(GenreDto::id)
                .containsExactlyInAnyOrderElementsOf(genresIds);
    }

    @Test
    @DisplayName("Должен создавать новую книгу")
    void insert_shouldCreateNewBook() {
        String title = "New Book Title";
        long authorId = 1L;
        Set<Long> genresIds = Set.of(1L, 2L);

        BookDto result = bookService.insert(title, authorId, genresIds);

        assertThat(result.id()).isPositive();
        assertThat(result.title()).isEqualTo(title);
        assertThat(result.author().id()).isEqualTo(authorId);
        assertThat(result.genres()).hasSize(2)
                .extracting(GenreDto::id)
                .containsExactlyInAnyOrderElementsOf(genresIds);
    }

    @Test
    @DisplayName("Должен бросать исключение при создании книги с пустым названием")
    void insert_shouldThrowForEmptyTitle() {
        assertThatThrownBy(() -> bookService.insert("", 1L, Set.of(1L)))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Title cannot be empty");
    }

    @Test
    @DisplayName("Должен бросать исключение при создании книги с пустым списком жанров")
    void insert_shouldThrowForEmptyGenres() {
        assertThatThrownBy(() -> bookService.insert("Title", 1L, Set.of()))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Genres cannot be empty");
    }

    @Test
    @DisplayName("Должен бросать исключение при создании книги с несуществующим автором")
    void insert_shouldThrowForNonExistingAuthor() {
        assertThatThrownBy(() -> bookService.insert("Title", 999L, Set.of(1L)))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Author with id 999 not found");
    }

    @Test
    @DisplayName("Должен бросать исключение при создании книги с несуществующими жанрами")
    void insert_shouldThrowForNonExistingGenres() {
        assertThatThrownBy(() -> bookService.insert("Title", 1L, Set.of(999L)))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Genres not found: [999]");
    }

    @Test
    @DisplayName("Должен бросать исключение при создании книги с частично несуществующими жанрами")
    void insert_shouldThrowForNonExistingSomeGenres() {
        assertThatThrownBy(() -> bookService.insert("Title", 1L, Set.of(1L, 999L)))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Genres not found: [999]");
    }

    @Test
    @DisplayName("Должен бросать исключение при обновлении книги с пустым названием")
    void update_shouldThrowForEmptyTitle() {
        assertThatThrownBy(() -> bookService.update(1L, "", 1L, Set.of(1L)))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Title cannot be empty");
    }

    @Test
    @DisplayName("Должен бросать исключение при обновлении книги с пустым списком жанров")
    void update_shouldThrowForEmptyGenres() {
        assertThatThrownBy(() -> bookService.update(1L, "Title", 1L, Set.of()))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Genres cannot be empty");
    }

    @Test
    @DisplayName("Должен бросать исключение при обновлении книги с несуществующим автором")
    void update_shouldThrowForNonExistingAuthor() {
        assertThatThrownBy(() -> bookService.update(1L, "Title", 999L, Set.of(1L)))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Author with id 999 not found");
    }

    @Test
    @DisplayName("Должен бросать исключение при обновлении книги с несуществующими жанрами")
    void update_shouldThrowForNonExistingGenres() {
        assertThatThrownBy(() -> bookService.update(1L, "Title", 1L, Set.of(999L)))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Genres not found: [999]");
    }

    @Test
    @DisplayName("Должен бросать исключение при обновлении книги с частично несуществующими жанрами")
    void update_shouldThrowForNonExistingSomeGenres() {
        assertThatThrownBy(() -> bookService.update(1L, "Title", 1L, Set.of(1L, 999L)))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Genres not found: [999]");
    }
}