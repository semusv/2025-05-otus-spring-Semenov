package ru.otus.hw.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;
import ru.otus.hw.dto.CommentDto;
import ru.otus.hw.models.Comment;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface CommentMapper {
    @Mapping(source = "book.id", target = "bookId")
    CommentDto toCommentDto(Comment comment);
}