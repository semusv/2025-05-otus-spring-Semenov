package ru.otus.hw.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;
import ru.otus.hw.dto.BookDto;
import ru.otus.hw.dto.BookUpdateDto;
import ru.otus.hw.dto.BookViewDto;
import ru.otus.hw.dto.CommentDto;
import ru.otus.hw.models.Book;

import java.util.List;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE,
        componentModel = MappingConstants.ComponentModel.SPRING,
        uses = {AuthorMapper.class, GenreMapper.class, CommentMapper.class})
public interface BookMapper {
    BookDto toBookDto(Book book);

    @Mapping(target = "comments", source = "book.comments")
    BookViewDto toBookViewDto(Book book);

    @Mapping(target = "comments", source = "commentDtos")
    BookViewDto toBookViewDto(BookDto bookDto,
                              List<CommentDto> commentDtos);

    // BookDto в BookUpdateDto
    @Mapping(target = "authorId", source = "bookDto.author.id")
    @Mapping(target = "genreIds", expression = "java(bookDto.genres().stream().map(GenreDto::id).toList())")
    BookUpdateDto toBookUpdateDto(BookDto bookDto);
}