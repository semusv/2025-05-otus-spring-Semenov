package ru.otus.hw.dto;

import lombok.Builder;

import java.util.List;

/**
 * DTO for {@link ru.otus.hw.models.Book}
 */
@Builder
public record BookFormDto(long id, String title, long authorId, List<Long> genreIds) {
}