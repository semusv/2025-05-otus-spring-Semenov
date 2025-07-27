package ru.otus.hw.services;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import ru.otus.hw.dto.BookDto;
import ru.otus.hw.dto.GenreDto;
import ru.otus.hw.exceptions.EntityNotFoundException;
import ru.otus.hw.mappers.AuthorMapper;
import ru.otus.hw.mappers.BookMapper;
import ru.otus.hw.mappers.CommentMapper;
import ru.otus.hw.mappers.GenreMapper;
import ru.otus.hw.models.Author;
import ru.otus.hw.models.Book;
import ru.otus.hw.models.Comment;
import ru.otus.hw.models.Genre;
import ru.otus.hw.services.mongo.listeners.BookCascadeDeleteListener;
import ru.otus.hw.services.providers.AuthorRepositoryProvider;
import ru.otus.hw.services.providers.GenreRepositoryProvider;
import ru.otus.hw.services.validators.BookValidatorImpl;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@Import({
        BookServiceImpl.class,
        BookMapper.class,
        AuthorMapper.class,
        GenreMapper.class,
        CommentMapper.class,
        AuthorRepositoryProvider.class,
        GenreRepositoryProvider.class,
        BookValidatorImpl.class,
        BookCascadeDeleteListener.class
})
class BookServiceImplTest extends BaseMongoTest {


    private final BookService bookService;

    @Autowired
    BookServiceImplTest(BookService bookService) {
        this.bookService = bookService;
    }

    @Test
    @DisplayName("Должен находить книгу по ID с автором и жанрами")
    void shouldFindBookByIdWithAuthorAndGenres() {
        // given
        Author author = createAuthor("Test Author");
        Genre genre1 = createGenre("Genre 1");
        Genre genre2 = createGenre("Genre 2");
        Book book = createBook("Test Book", author, List.of(genre1, genre2));

        // when
        Optional<BookDto> result = bookService.findById(book.getId());

        // then
        assertThat(result).isPresent();
        assertAll(
                () -> assertThat(result.get())
                        .extracting(BookDto::title, dto -> dto.author().fullName())
                        .containsExactly("Test Book", "Test Author"),
                () -> assertThat(result.get().genres())
                        .extracting(GenreDto::name)
                        .containsExactlyInAnyOrder("Genre 1", "Genre 2")
        );
    }

    @Test
    @DisplayName("Должен возвращать все книги с авторами и жанрами")
    void shouldFindAllBooksWithAuthorsAndGenres() {
        // given
        Author author1 = createAuthor("Author 1");
        Author author2 = createAuthor("Author 2");
        Genre genre = createGenre("Common Genre");

        createBook("Book 1", author1, List.of(genre));
        createBook("Book 2", author2, List.of(genre));

        // when
        List<BookDto> result = bookService.findAll();

        // then
        assertAll(
                () -> assertThat(result).hasSize(2),
                () -> assertThat(result)
                        .extracting(BookDto::title)
                        .containsExactlyInAnyOrder("Book 1", "Book 2"),
                () -> assertThat(result)
                        .extracting(dto -> dto.author().fullName())
                        .containsExactlyInAnyOrder("Author 1", "Author 2"),
                () -> assertThat(result)
                        .flatExtracting(BookDto::genres)
                        .extracting(GenreDto::name)
                        .containsOnly("Common Genre")
        );
    }

    @Test
    @DisplayName("Должен создавать новую книгу с автором и жанрами")
    void shouldCreateBookWithAuthorAndGenres() {
        // given
        Author author = createAuthor("New Author");
        Genre genre1 = createGenre("Genre A");
        Genre genre2 = createGenre("Genre B");

        // when
        BookDto result = bookService.insert(
                "New Book Title",
                author.getId(),
                Set.of(genre1.getId(), genre2.getId())
        );

        // then
        Book savedBook = mongoTemplate.findById(result.id(), Book.class);
        assertAll(
                () -> assertThat(result.title()).isEqualTo("New Book Title"),
                () -> assertThat(result.author().id()).isEqualTo(author.getId()),
                () -> assertThat(result.genres()).hasSize(2),
                () -> assertThat(savedBook).isNotNull(),
                () -> {
                    assert savedBook != null;
                    assertThat(savedBook.getAuthor().getId()).isEqualTo(author.getId());
                },
                () -> {
                    assert savedBook != null;
                    assertThat(savedBook.getGenres())
                            .extracting(Genre::getId)
                            .containsExactlyInAnyOrder(genre1.getId(), genre2.getId());
                }
        );
    }

    @Test
    @DisplayName("Должен обновлять книгу с изменением автора и жанров")
    void shouldUpdateBookWithNewAuthorAndGenres() {
        // given
        Author oldAuthor = createAuthor("Old Author");
        Author newAuthor = createAuthor("New Author");
        Genre oldGenre = createGenre("Old Genre");
        Genre newGenre = createGenre("New Genre");
        Book book = createBook("Old Title", oldAuthor, List.of(oldGenre));

        // when
        BookDto result = bookService.update(
                book.getId(),
                "New Title",
                newAuthor.getId(),
                Set.of(newGenre.getId())
        );

        // then
        Book updatedBook = mongoTemplate.findById(book.getId(), Book.class);
        assertAll(
                () -> assertThat(result.title()).isEqualTo("New Title"),
                () -> assertThat(result.author().id()).isEqualTo(newAuthor.getId()),
                () -> assertThat(result.genres())
                        .extracting(GenreDto::name)
                        .containsExactly("New Genre"),
                () -> {
                    assert updatedBook != null;
                    assertThat(updatedBook.getTitle()).isEqualTo("New Title");
                },
                () -> {
                    assert updatedBook != null;
                    assertThat(updatedBook.getAuthor().getId()).isEqualTo(newAuthor.getId());
                },
                () -> {
                    assert updatedBook != null;
                    assertThat(updatedBook.getGenres())
                            .extracting(Genre::getId)
                            .containsExactly(newGenre.getId());
                }
        );
    }

    @Test
    @DisplayName("Должен удалять книгу и связанные комментарии")
    void shouldDeleteBookWithCascade() {
        // given
        Author author = createAuthor("Test Author");
        Book book = createBook("To Delete", author, List.of());
        Comment comment1 = createComment("Comment 1", book);
        Comment comment2 = createComment("Comment 2", book);

        // when
        bookService.deleteById(book.getId());

        // then
        assertThat(mongoTemplate.findById(book.getId(), Book.class)).isNull();
        assertThat(mongoTemplate.findById(comment1.getId(), Comment.class)).isNull();
        assertThat(mongoTemplate.findById(comment2.getId(), Comment.class)).isNull();

    }

    @Test
    @DisplayName("Должен бросать исключение при удалении несуществующей книги")
    void shouldThrowWhenDeletingNonExistentBook() {
        assertThrows(EntityNotFoundException.class,
                () -> bookService.deleteById("nonexistent_id"));
    }
}