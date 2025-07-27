package ru.otus.hw.services;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import ru.otus.hw.dto.GenreDto;
import ru.otus.hw.mappers.GenreMapper;
import ru.otus.hw.models.Genre;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

@Import({
        GenreMapper.class,
        GenreServiceImpl.class
})
class GenreServiceImplTest extends BaseMongoTest {

    private final GenreService genreService;

    @Autowired
    GenreServiceImplTest(GenreService genreService) {
        this.genreService = genreService;
    }

    @Test
    @DisplayName("Должен возвращать все жанры")
    void shouldFindAllGenres() {
        // given
        Genre genre1 = createGenre("Fantasy");
        Genre genre2 = createGenre("Science Fiction");

        // when
        List<GenreDto> result = genreService.findAll();

        // then
        assertAll(
                () -> assertThat(result).hasSize(2),
                () -> assertThat(result)
                        .extracting(GenreDto::name)
                        .containsExactlyInAnyOrder("Fantasy", "Science Fiction"),
                () -> assertThat(result)
                        .extracting(GenreDto::id)
                        .containsExactlyInAnyOrder(genre1.getId(), genre2.getId())
        );
    }

    @Test
    @DisplayName("Должен возвращать пустой список, когда жанров нет")
    void shouldReturnEmptyListWhenNoGenres() {
        List<GenreDto> result = genreService.findAll();
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Должен находить жанры по IDs")
    void shouldFindGenresByIds() {
        // given
        Genre genre1 = createGenre("Genre 1");
        Genre genre2 = createGenre("Genre 2");
        createGenre("Genre 3"); // Не должен быть найден

        // when
        List<GenreDto> result = genreService.findAllByIds(Set.of(
                genre1.getId(),
                genre2.getId()
        ));

        // then
        assertAll(
                () -> assertThat(result).hasSize(2),
                () -> assertThat(result)
                        .extracting(GenreDto::name)
                        .containsExactlyInAnyOrder("Genre 1", "Genre 2")
        );
    }

    @Test
    @DisplayName("Должен возвращать пустой список для пустого набора IDs")
    void shouldReturnEmptyListForEmptyIds() {
        // given
        createGenre("Genre 1");
        createGenre("Genre 2");

        // when
        List<GenreDto> result = genreService.findAllByIds(Set.of());

        // then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Должен возвращать пустой список для null IDs")
    void shouldReturnEmptyListForNullIds() {
        // given
        createGenre("Genre 1");

        // when
        List<GenreDto> result = genreService.findAllByIds(null);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Должен возвращать только существующие жанры")
    void shouldReturnOnlyExistingGenres() {
        // given
        Genre existingGenre = createGenre("Existing Genre");
        String nonExistentId = "507f1f77bcf86cd799439011";

        // when
        List<GenreDto> result = genreService.findAllByIds(Set.of(
                existingGenre.getId(),
                nonExistentId
        ));

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).name()).isEqualTo("Existing Genre");
    }
}