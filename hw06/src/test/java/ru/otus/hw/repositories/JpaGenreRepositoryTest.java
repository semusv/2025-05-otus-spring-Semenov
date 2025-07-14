package ru.otus.hw.repositories;

import lombok.val;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;


@DisplayName("Репозиторий на основе Jpa для работы с жанрами ")
@DataJpaTest
@Import(JpaGenreRepository.class)
class JpaGenreRepositoryTest {


    private static final long FIRST_GENRE_ID = 1L;
    private static final long SECOND_GENRE_ID = 2L;
    private static final long NON_EXIST_GENRE_ID = 999L;
    private static final int EXPECTED_NUMBER_OF_GENRES = 6;

    @Autowired
    JpaGenreRepository repositoryJpa;

    @DisplayName("Должен загружать список всех жанров")
    @Test
    void shouldFindAllGenres() {
        // when
        val genres = repositoryJpa.findAll();

        // then
        assertThat(genres)
                .isNotNull()
                .hasSize(EXPECTED_NUMBER_OF_GENRES)
                .allSatisfy(genre -> {
                    assertThat(genre.getId()).isPositive();
                    assertThat(genre.getName()).isNotBlank();
                });
    }

    @DisplayName("Должен загружать список жанров по их id")
    @Test
    void shouldFindAllByIds() {
        // given
        val ids = Set.of(FIRST_GENRE_ID, SECOND_GENRE_ID);

        // when
        val genres = repositoryJpa.findAllByIds(ids);

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
        val genres = repositoryJpa.findAllByIds(ids);

        // then
        assertThat(genres)
                .isNotNull()
                .isEmpty();
    }

    @DisplayName("Должен возвращать только существующие жанры")
    @Test
    void shouldReturnOnlyExistingGenres() {
        // given
        val ids = Set.of(FIRST_GENRE_ID, NON_EXIST_GENRE_ID);

        // when
        val genres = repositoryJpa.findAllByIds(ids);

        // then
        assertThat(genres)
                .isNotNull()
                .hasSize(1)
                .allSatisfy(genre -> {
                    assertThat(genre.getId()).isEqualTo(FIRST_GENRE_ID);
                    assertThat(genre.getName()).isNotBlank();
                });
    }
}