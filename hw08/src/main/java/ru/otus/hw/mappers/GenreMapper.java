package ru.otus.hw.mappers;

import org.springframework.stereotype.Component;
import ru.otus.hw.dto.GenreDto;
import ru.otus.hw.models.Genre;

@Component
public class GenreMapper {
    public String dtoToString(GenreDto genre) {
        return "Id: %s, Name: %s".formatted(genre.id(), genre.name());
    }

    public GenreDto toDto(Genre genre) {
        if (genre != null) {
            return new GenreDto(genre.getId(), genre.getName());
        } else {
            return null;
        }
    }
}
