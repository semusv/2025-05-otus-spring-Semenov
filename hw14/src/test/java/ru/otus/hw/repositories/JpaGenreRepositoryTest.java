package ru.otus.hw.repositories;

import lombok.val;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import ru.otus.hw.models.h2.Genre;
import ru.otus.hw.repositories.jpa.GenreRepository;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;


@DisplayName("Репозиторий на основе Jpa для работы с жанрами ")
@DataJpaTest
class JpaGenreRepositoryTest {


    private static final long NON_EXIST_GENRE_ID = 999L;
    private static final int EXPECTED_NUMBER_OF_GENRES = 3;

    @Autowired
    GenreRepository repositoryJpa;

    @Autowired
    TestEntityManager em;

    @DisplayName("Должен загружать список всех жанров")
    @Test
    void shouldFindAllGenres() {
        //given
        em.persist(new Genre(0, "Genre_001"));
        em.persist(new Genre(0, "Genre_002"));
        em.persist(new Genre(0, "Genre_003"));
        em.persist(new Genre(0, "Genre_004"));
        em.flush();
        em.clear();

        // when
        val genres = repositoryJpa.findAll();

        // then
        assertThat(genres)
                .isNotNull()
                .hasSizeGreaterThan(EXPECTED_NUMBER_OF_GENRES)
                .allSatisfy(genre -> {
                    assertThat(genre.getId()).isPositive();
                    assertThat(genre.getName()).isNotBlank();
                });
    }

    @DisplayName("Должен загружать список жанров по их id")
    @Test
    void shouldFindAllByIds() {
        // given
        Genre firstGenre = em.persist(new Genre(0, "Genre_001"));
        Genre secondGenre = em.persist(new Genre(0, "Genre_002"));
        val ids = Set.of(firstGenre.getId(), secondGenre.getId());
        em.flush();
        em.clear();

        // when
        val genres = repositoryJpa.findAllById(ids);

        // then
        assertThat(genres)
                .isNotNull()
                .hasSize(2)
                .allSatisfy(genre -> {
                    assertThat(genre.getId()).isIn(ids);
                    assertThat(genre.getName()).isNotBlank();
                });
    }


    @DisplayName("Должен возвращать пустой список, если жанры не найдены")
    @Test
    void shouldReturnEmptyListWhenGenresNotFound() {
        // given
        val ids = Set.of(NON_EXIST_GENRE_ID);

        // when
        val genres = repositoryJpa.findAllById(ids);

        // then
        assertThat(genres)
                .isNotNull()
                .isEmpty();
    }

    @DisplayName("Должен возвращать только существующие жанры")
    @Test
    void shouldReturnOnlyExistingGenres() {
        // given
        Genre firstGenre = em.persist(new Genre(0, "Genre_001"));
        val ids = Set.of(firstGenre.getId(), NON_EXIST_GENRE_ID);
        em.flush();
        em.clear();

        // when
        val genres = repositoryJpa.findAllById(ids);

        // then
        assertThat(genres)
                .isNotNull()
                .hasSize(1)
                .allSatisfy(genre -> {
                    assertThat(genre.getId()).isEqualTo(firstGenre.getId());
                    assertThat(genre.getName()).isNotBlank();
                });
    }
}