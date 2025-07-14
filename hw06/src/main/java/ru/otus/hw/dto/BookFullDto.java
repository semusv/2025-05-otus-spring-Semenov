package ru.otus.hw.dto;

import java.util.List;

public record BookFullDto(long id, String title, AuthorDto author, List<GenreDto> genres, List<CommentDto> comments) {
}
