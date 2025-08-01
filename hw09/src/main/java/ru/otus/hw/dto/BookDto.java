package ru.otus.hw.dto;

import lombok.Builder;

import java.util.List;

@Builder
public record BookDto(long id, String title, AuthorDto author, List<GenreDto> genres) {
}
