package ru.otus.hw.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CommentDto(
        Long id,

        @NotNull(message = "{validation.comment.text.notNull}")
        @Size(min = 5, max = 500, message = "{validation.comment.text.size}")
        String text,

        @NotNull(message = "{validation.book.id.notnull}")
        Long bookId) {
}
