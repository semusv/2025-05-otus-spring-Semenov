package ru.otus.hw.converters;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.otus.hw.dto.BookShortDto;
import ru.otus.hw.dto.CommentDto;
import ru.otus.hw.models.Comment;

@Component
@RequiredArgsConstructor
public class CommentDtoConverter {

    private final AuthorDtoConverter authorDtoConverter;

    private final BookShortDtoConverter bookShortDtoConverter;

    public String commentDtoToString(CommentDto commentDto) {
        return "Id: %d, Text: %s, for book: %s".formatted(
                commentDto.id(),
                commentDto.text(),
                bookShortDtoConverter.bookShortDtoToString(commentDto.book()));
    }

    public CommentDto commentToDto(Comment comment) {
        if (comment != null) {
            return new CommentDto(
                    comment.getId(),
                    comment.getText(),
                    new BookShortDto(
                            comment.getBook().getId(),
                            comment.getBook().getTitle(),
                            authorDtoConverter.authorToDto(comment.getBook().getAuthor())));
        } else {
            return null;
        }
    }
}
