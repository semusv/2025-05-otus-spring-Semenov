package ru.otus.hw.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;
import ru.otus.hw.dto.BookDto;
import ru.otus.hw.dto.GenreDto;
import ru.otus.hw.dto.api.BookCreateDto;
import ru.otus.hw.dto.api.BookUpdateDto;
import ru.otus.hw.models.Author;
import ru.otus.hw.models.Book;
import ru.otus.hw.models.Genre;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE,
        componentModel = MappingConstants.ComponentModel.SPRING,
        uses = {AuthorMapper.class, GenreMapper.class, CommentMapper.class})
public interface BookMapper {
    BookDto toBookDto(Book book);

    @Mapping(target = "author", source = "authorId", qualifiedByName = "mapAuthorIdToAuthor")
    @Mapping(target = "genres", source = "genreIds", qualifiedByName = "mapGenreIdsToGenres")
    @Mapping(target = "comments", ignore = true)
    Book toBook(BookCreateDto dto);

    @Mapping(target = "author", source = "authorId", qualifiedByName = "mapAuthorIdToAuthor")
    @Mapping(target = "genres", source = "genreIds", qualifiedByName = "mapGenreIdsToGenres")
    @Mapping(target = "comments", ignore = true)
    Book toBook(BookUpdateDto dto);


    @Named("mapAuthorIdToAuthor")
    default Author mapAuthorIdToAuthor(Long authorId) {
        if (authorId == null) {
            return null;
        }
        Author author = new Author();
        author.setId(authorId);
        return author;
    }

    @Named("mapGenreIdsToGenres")
    default List<Genre> mapGenreIdsToGenres(Set<Long> genreIds) {
        if (genreIds == null) {
            return List.of();
        }
        return genreIds.stream()
                .map(id -> {
                    Genre genre = new Genre();
                    genre.setId(id);
                    return genre;
                }).toList();
    }

    @SuppressWarnings("unused")
    @Named("mapGenresDtoToGenreIDs")
    default Set<Long> mapGenresDtoToGenreIDs(List<GenreDto> genres) {
        if (genres == null) {
            return Collections.emptySet();
        }
        return genres.stream().map(GenreDto::id).collect(Collectors.toSet());
    }
}