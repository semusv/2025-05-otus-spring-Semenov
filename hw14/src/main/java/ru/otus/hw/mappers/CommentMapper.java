package ru.otus.hw.mappers;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.otus.hw.dto.CommentDto;
import ru.otus.hw.models.h2.Comment;

@Component
@RequiredArgsConstructor
public class CommentMapper {

    public String commentDtoToString(CommentDto commentDto) {
        return "Id: %d, Text: %s, for bookId: %s".formatted(
                commentDto.id(),
                commentDto.text(),
                commentDto.bookId()
        );
    }

    public CommentDto commentToDto(Comment comment) {
        if (comment != null) {
            return new CommentDto(
                    comment.getId(),
                    comment.getText(),
                    comment.getBook().getId());
        } else {
            return null;
        }
    }
}
