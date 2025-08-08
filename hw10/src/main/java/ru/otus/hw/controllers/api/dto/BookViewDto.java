package ru.otus.hw.controllers.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import ru.otus.hw.dto.AuthorDto;
import ru.otus.hw.dto.CommentDto;
import ru.otus.hw.dto.GenreDto;

import java.util.List;

/**
 * DTO for {@link ru.otus.hw.models.Book}
 */
@Builder
public record BookViewDto(
        @NotNull(message = "{validation.book.id.notnull}")
        Long id,

        @NotBlank(message = "{validation.book.title.not-blank}")
        @Size(max = 100, message = "{validation.book.title.size}")
        String title,

        @NotNull(message = "{validation.book.author.notnull}")
        AuthorDto author,

        @Size(min = 1, message = "{validation.book.genres.size}")
        List<GenreDto> genres,

        List<CommentDto> comments
) {
}