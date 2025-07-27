package ru.otus.hw.mappers;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.otus.hw.dto.CommentDto;
import ru.otus.hw.models.Comment;

@Component
@RequiredArgsConstructor
public class CommentMapper {

    public String dtoToString(CommentDto commentDto) {
        return "Id: %s, Text: %s, for bookId: %s".formatted(
                commentDto.id(),
                commentDto.text(),
                commentDto.bookId()
        );
    }

    public CommentDto toDto(Comment comment) {
        if (comment != null) {
            return new CommentDto(
                    comment.getId(),
                    comment.getText(),
                    comment.getBookId());
        } else {
            return null;
        }
    }
}
