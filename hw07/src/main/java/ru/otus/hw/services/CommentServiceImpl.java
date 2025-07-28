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
import ru.otus.hw.services.validators.CommentValidator;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class CommentServiceImpl implements CommentService {

    private final CommentMapper commentDtoConverter;

    private final CommentRepository commentRepository;

    private final BookRepository bookRepository;

    private final CommentValidator commentValidator;

    @Override
    @Transactional(readOnly = true)
    public Optional<CommentDto> findById(long id) {
        return commentRepository.findById(id).map(commentDtoConverter::commentToDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommentDto> findByBookId(long bookId) {
        validateBookExists(bookId);
        return commentRepository.findByBookId(bookId).stream()
                .map(commentDtoConverter::commentToDto)
                .toList();
    }

    @Override
    @Transactional
    public void deleteById(long id) {
        if (!commentRepository.existsById(id)) {
            throw new EntityNotFoundException("Comment with id %d not found".formatted(id));
        }
        commentRepository.deleteById(id);
    }

    @Override
    @Transactional
    public CommentDto insert(String text, long bookId) {
        var comment = new Comment();
        commentValidator.validateText(text);
        comment.setText(text);
        var book = getBookById(bookId);
        comment.setBook(book);
        return commentDtoConverter.commentToDto(commentRepository.save(comment));
    }

    @Override
    @Transactional
    public CommentDto update(long id, String text) {
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Comment with id %d not found".formatted(id)));
        commentValidator.validateText(text);
        comment.setText(text);
        return commentDtoConverter.commentToDto(commentRepository.save(comment));
    }

    private Book getBookById(long id) throws EntityNotFoundException {
        return bookRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Book with id %d not found".formatted(id)));
    }

    private void validateBookExists(long id) throws EntityNotFoundException {
        if (!bookRepository.existsById(id)) {
            throw new EntityNotFoundException("Book with id %d not found".formatted(id));
        }
    }
}
