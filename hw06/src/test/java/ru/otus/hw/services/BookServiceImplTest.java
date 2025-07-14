package ru.otus.hw.services;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import ru.otus.hw.converters.*;
import ru.otus.hw.dto.BookFullDto;
import ru.otus.hw.dto.GenreDto;
import ru.otus.hw.exceptions.EntityNotFoundException;
import ru.otus.hw.repositories.JpaAuthorRepository;
import ru.otus.hw.repositories.JpaBookRepository;
import ru.otus.hw.repositories.JpaCommentRepository;
import ru.otus.hw.repositories.JpaGenreRepository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

@DisplayName("Интеграционный тест для сервиса книг")
@DataJpaTest
@Import({
        BookServiceImpl.class,
        BookFullDtoConverter.class,
        AuthorDtoConverter.class,
        GenreDtoConverter.class,
        CommentDtoConverter.class,
        JpaAuthorRepository.class,
        JpaGenreRepository.class,
        JpaBookRepository.class,
        JpaCommentRepository.class,
        BookShortDtoConverter.class
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
        Optional<BookFullDto> result = bookService.findById(1L);

        assertThat(result).isPresent();
        BookFullDto book = result.get();

        assertThat(book.id()).isPositive();
        assertThat(book.title()).isNotBlank();

        assertThat(book.author()).isNotNull();
        assertThat(book.author().fullName()).isNotBlank();

        assertThat(book.genres()).isNotEmpty();
        book.genres().forEach(genre -> {
            assertThat(genre.id()).isPositive();
            assertThat(genre.name()).isNotBlank();
        });

        assertThat(book.comments()).isNotNull();
    }

    @Test
    @DisplayName("Должен возвращать пустой Optional при поиске несуществующей книги")
    void findById_shouldReturnEmptyForNonExistingId() {
        Optional<BookFullDto> result = bookService.findById(999L);
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Должен возвращать список всех книг с полной информацией")
    void findAll_shouldReturnAllBooksWithFullInfo() {
        List<BookFullDto> result = bookService.findAll();

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
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    void deleteById_shouldDeleteBook() {
        long bookId = 1L;
        assertThat(bookService.findById(bookId)).isPresent();

        bookService.deleteById(bookId);

        assertThat(bookService.findById(bookId)).isEmpty();
    }

    @Test
    @DisplayName("Должен создавать новую книгу")
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    void insert_shouldCreateNewBook() {
        String title = "New Book Title";
        long authorId = 1L;
        Set<Long> genresIds = Set.of(1L, 2L);

        BookFullDto result = bookService.insert(title, authorId, genresIds);

        assertThat(result.id()).isPositive();
        assertThat(result.title()).isEqualTo(title);
        assertThat(result.author().id()).isEqualTo(authorId);
        assertThat(result.genres()).hasSize(2)
                .extracting(GenreDto::id)
                .containsExactlyInAnyOrderElementsOf(genresIds);
    }

    @Test
    @DisplayName("Должен обновлять существующую книгу")
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    void update_shouldUpdateExistingBook() {
        long bookId = 1L;
        String updatedTitle = "Updated Title";
        long authorId = 1L;
        Set<Long> genresIds = Set.of(1L, 2L);

        BookFullDto result = bookService.update(bookId, updatedTitle, authorId, genresIds);

        assertThat(result.id()).isEqualTo(bookId);
        assertThat(result.title()).isEqualTo(updatedTitle);
        assertThat(result.author().id()).isEqualTo(authorId);
        assertThat(result.genres()).hasSize(2)
                .extracting(GenreDto::id)
                .containsExactlyInAnyOrderElementsOf(genresIds);
    }

    @Test
    @DisplayName("Должен бросать исключение при создании книги с пустым названием")
    void insert_shouldThrowForEmptyTitle() {
        assertThatThrownBy(() -> bookService.insert("", 1L, Set.of(1L)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Title must not be empty");
    }

    @Test
    @DisplayName("Должен бросать исключение при создании книги с пустым списком жанров")
    void insert_shouldThrowForEmptyGenres() {
        assertThatThrownBy(() -> bookService.insert("Title", 1L, Set.of()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Genres ids must not be null");
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
                .hasMessageContaining("One or all genres with ids [999] not found");
    }
}