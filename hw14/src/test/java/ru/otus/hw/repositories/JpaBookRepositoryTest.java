package ru.otus.hw.repositories;


import lombok.val;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import ru.otus.hw.models.h2.Author;
import ru.otus.hw.models.h2.Book;
import ru.otus.hw.models.h2.Genre;
import ru.otus.hw.repositories.jpa.BookRepository;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Репозиторий на основе Jpa для работы с книгами ")
@DataJpaTest
class JpaBookRepositoryTest {

    private static final long FIRST_BOOK_ID = 1L;
    private static final long THIRD_GENRE_ID = 3L;
    private static final long SECOND_AUTHOR_ID = 2L;
    private static final int EXPECTED_NUMBER_OF_BOOKS = 3;
    private static final long NON_EXIST_BOOK_ID = 999L;

    @Autowired
    BookRepository repositoryJpa;

    @Autowired
    TestEntityManager em;


    @DisplayName("Должен загружать информацию о нужной книге по ее id")
    @Test
    void shouldFindExpectedBookById() {
        // given
        val expectedBook = em.find(Book.class, FIRST_BOOK_ID);
        val sizeGenres = expectedBook.getGenres().size();
        em.detach(expectedBook); // Отсоединяем, чтобы гарантировать загрузку из БД

        //when
        val optionalActualBook = repositoryJpa.findById(FIRST_BOOK_ID);
        // then
        assertThat(optionalActualBook)
                .isPresent()
                .get()
                .satisfies(book -> {
                    // Проверка основных полей
                    assertThat(book)
                            .extracting(
                                    Book::getId,
                                    Book::getTitle,
                                    b -> b.getAuthor().getId(),
                                    b -> b.getGenres().size()
                            )
                            .containsExactly(
                                    expectedBook.getId(),
                                    expectedBook.getTitle(),
                                    expectedBook.getAuthor().getId(),
                                    sizeGenres
                            );
                    assertThat(book.getComments())
                            .isNotNull()
                            .isInstanceOf(List.class)
                            .hasSizeGreaterThanOrEqualTo(0);
                });

    }

    @DisplayName("Должен возвращать пустой Optional, если книга не найдена")
    @Test
    void shouldReturnEmptyOptionalWhenAuthorNotFound() {
        //when
        val optionalBook = repositoryJpa.findById(NON_EXIST_BOOK_ID);
        //then
        assertThat(optionalBook).isEmpty();
    }

    @DisplayName("Должен загружать список всех книг с полной информацией о них")
    @Test
    void shouldReturnCorrectBooksListWithAllInfo() {
        // when
        List<Book> books = repositoryJpa.findAll();

        // then
        assertThat(books)
                .isNotNull()
                .hasSize(EXPECTED_NUMBER_OF_BOOKS)
                .allSatisfy(book -> {
                    assertThat(book.getTitle()).isNotBlank();
                    assertThat(book.getAuthor())
                            .isNotNull()
                            .extracting(Author::getId, Author::getFullName)
                            .doesNotContainNull();
                    assertThat(book.getGenres())
                            .hasSize(2)
                            .allMatch(genre -> genre.getId() > 0)
                            .allMatch(genre -> !genre.getName().isBlank());
                    assertThat(book.getComments())
                            .isNotNull()
                            .isInstanceOf(List.class)
                            .hasSizeGreaterThanOrEqualTo(0);
                });
    }

    @DisplayName("Должен корректно обновлять существующую книгу")
    @Test
    void shouldUpdateExistingBook() {
        //given
        Author newAuthor = em.find(Author.class, SECOND_AUTHOR_ID);
        Genre newGenre = em.find(Genre.class, THIRD_GENRE_ID);
        Book existingBook = em.find(Book.class, FIRST_BOOK_ID);
        String oldTitle = existingBook.getTitle();
        Set<Genre> initialGenres = new HashSet<>(existingBook.getGenres());
        existingBook.setTitle("Updated Title");
        existingBook.setAuthor(newAuthor);
        existingBook.getGenres().add(newGenre);
        em.detach(existingBook);

        //when
        repositoryJpa.save(existingBook);
        em.flush();
        em.clear();

        //then
        val foundBook = em.find(Book.class, FIRST_BOOK_ID);
        assertThat(foundBook).isNotNull();
        assertThat(foundBook.getTitle())
                .isNotEqualTo(oldTitle)
                .isEqualTo("Updated Title");
        assertThat(foundBook.getAuthor().getId()).isEqualTo(newAuthor.getId());
        assertThat(foundBook.getGenres())
                .hasSize(initialGenres.size() + 1)
                .anyMatch(g -> g.getId() == newGenre.getId());
    }

    @DisplayName("Должен корректно добавлять новую книгу")
    @Test
    void shouldInsertNewBook() {
        // given
        Author author = em.find(Author.class, SECOND_AUTHOR_ID);
        Genre genre = em.find(Genre.class, THIRD_GENRE_ID);
        var newBook = new Book(0, "New Book", author, List.of(genre), List.of());

        // when
        Book savedBook = repositoryJpa.save(newBook);
        em.flush();
        em.clear();

        // then
        val foundBook = em.find(Book.class, savedBook.getId());
        assertThat(foundBook).isNotNull();
        assertThat(foundBook.getTitle()).contains("New Book");

        assertThat(foundBook.getAuthor())
                .matches(a -> a.getId() == SECOND_AUTHOR_ID)
                .matches(a -> a.getFullName().equals(author.getFullName()));
        assertThat(foundBook.getGenres()).hasSize(1)
                .first()
                .matches(g -> g.getId() == THIRD_GENRE_ID)
                .matches(g -> g.getName().equals(genre.getName()));

    }


    @DisplayName("Должен удалять книгу по id")
    @Test
    void shouldDeleteBookById() {
        //given
        Book existingBook = em.find(Book.class, FIRST_BOOK_ID);
        assertThat(existingBook).isNotNull();
        em.detach(existingBook);

        //when
        repositoryJpa.deleteById(FIRST_BOOK_ID);
        em.flush();
        em.clear();

        //then
        assertThat(em.find(Book.class, FIRST_BOOK_ID)).isNull();
    }


    @DisplayName("Должен возвращать true если книги не существует")
    @Test
    void shouldReturnTrueWhenBookNotExists() {
        //when
        boolean result = !repositoryJpa.existsById(NON_EXIST_BOOK_ID);
        //then
        assertThat(result).isTrue();
    }

    @DisplayName("Должен возвращать false если книга существует")
    @Test
    void shouldReturnFalseWhenBookExists() {
        //when
        boolean result = !repositoryJpa.existsById(FIRST_BOOK_ID);
        //then
        assertThat(result).isFalse();
    }

}