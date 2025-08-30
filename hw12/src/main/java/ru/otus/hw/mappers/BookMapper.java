package ru.otus.hw.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;
import ru.otus.hw.dto.BookDto;
import ru.otus.hw.dto.GenreDto;
import ru.otus.hw.dto.api.BookUpdateDto;
import ru.otus.hw.models.Book;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@SuppressWarnings("unused")
@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE,
        componentModel = MappingConstants.ComponentModel.SPRING,
        uses = {AuthorMapper.class, GenreMapper.class, CommentMapper.class})
public interface BookMapper {
    BookDto toBookDto(Book book);

    // BookDto в BookUpdateDto
    @Mapping(target = "authorId", source = "bookDto.author.id")
    @Mapping(target = "genreIds", source = "bookDto.genres")
    BookUpdateDto toBookUpdateDto(BookDto bookDto);

    default Set<Long> mapGenres(List<GenreDto> genres) {
        if (genres == null) {
            return Collections.emptySet();
        }
        return genres.stream().map(GenreDto::id).collect(Collectors.toSet());
    }
}