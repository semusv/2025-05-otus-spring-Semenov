package ru.otus.hw.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.otus.hw.mappers.CommentMapper;
import ru.otus.hw.dto.CommentDto;
import ru.otus.hw.exceptions.EntityNotFoundException;
import ru.otus.hw.models.Book;
import ru.otus.hw.models.Comment;
import ru.otus.hw.repositories.BookRepository;
import ru.otus.hw.repositories.CommentRepository;


import java.util.List;

@RequiredArgsConstructor
@Service
public class CommentServiceImpl implements CommentService {

    private static final String COMMENT_WITH_ID_D_NOT_FOUND = "Comment with id %d not found";

    private final CommentMapper commentDtoConverter;

    private final CommentRepository commentRepository;

    private final BookRepository bookRepository;

    @Override
    @Transactional(readOnly = true)
    public CommentDto findById(long id) {
        return commentRepository.findById(id)
                .map(commentDtoConverter::toCommentDto)
                .orElseThrow(() ->
                        new EntityNotFoundException(
                                COMMENT_WITH_ID_D_NOT_FOUND.formatted(id),
                                "exception.entity.not.found.comment",
                                id)
                );
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommentDto> findByBookId(long bookId) {
        validateBookExists(bookId);
        return commentRepository.findByBookId(bookId).stream()
                .map(commentDtoConverter::toCommentDto)
                .toList();
    }

    @Override
    @Transactional
    public void deleteById(long id) {
        if (!commentRepository.existsById(id)) {
            throw new EntityNotFoundException(
                    COMMENT_WITH_ID_D_NOT_FOUND.formatted(id),
                    "exception.entity.not.found.comment",
                    id);
        }
        commentRepository.deleteById(id);
    }

    @Override
    @Transactional
    public CommentDto insert(CommentDto commentDto) {
        var comment = new Comment();
        comment.setText(commentDto.text());
        var book = getBookById(commentDto.bookId());
        comment.setBook(book);
        return commentDtoConverter.toCommentDto(commentRepository.save(comment));
    }

    @Override
    @Transactional
    public CommentDto update(CommentDto commentDto) {
        Comment comment = commentRepository.findById(commentDto.id())
                .orElseThrow(() ->
                        new EntityNotFoundException(
                                COMMENT_WITH_ID_D_NOT_FOUND.formatted(commentDto.id()),
                                "exception.entity.not.found.comment",
                                commentDto.id())
                );
        comment.setText(commentDto.text());
        return commentDtoConverter.toCommentDto(commentRepository.save(comment));
    }

    private Book getBookById(long id) throws EntityNotFoundException {
        return bookRepository.findById(id)
                .orElseThrow(() ->
                        new EntityNotFoundException(
                                "Book with id %d not found".formatted(id),
                                "exception.entity.not.found.book",
                                id)
                );
    }

    private void validateBookExists(long id) throws EntityNotFoundException {
        if (!bookRepository.existsById(id)) {
            throw new EntityNotFoundException(
                    "Book with id %d not found".formatted(id),
                    "exception.entity.not.found.book",
                    id);
        }
    }
}
