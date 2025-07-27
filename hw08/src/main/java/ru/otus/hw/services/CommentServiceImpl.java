package ru.otus.hw.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.otus.hw.mappers.CommentMapper;
import ru.otus.hw.dto.CommentDto;
import ru.otus.hw.exceptions.EntityNotFoundException;
import ru.otus.hw.models.Comment;
import ru.otus.hw.repositories.CommentRepository;
import ru.otus.hw.services.providers.BookProvider;
import ru.otus.hw.services.validators.CommentValidator;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class CommentServiceImpl implements CommentService {

    private final CommentMapper commentDtoConverter;

    private final CommentRepository commentRepository;

    private final BookProvider bookProvider;

    private final CommentValidator commentValidator;

    @Override
    public Optional<CommentDto> findById(String id) {
        return commentRepository.findById(id).map(commentDtoConverter::toDto);
    }

    @Override
    public List<CommentDto> findByBookId(String bookId) {
        bookProvider.validateBookExists(bookId);
        return commentRepository.findByBookId(bookId).stream()
                .map(commentDtoConverter::toDto)
                .toList();
    }

    @Override
    public void deleteById(String id) {
        if (!commentRepository.existsById(id)) {
            throw new EntityNotFoundException("Comment with id %s not found".formatted(id));
        }
        commentRepository.deleteById(id);
    }

    @Override
    public CommentDto insert(String text, String bookId) {
        commentValidator.validateText(text);
        var comment = new Comment();
        comment.setText(text);
        var book = bookProvider.fetchBook(bookId);
        comment.setBook(book);
        comment.setBookId(book.getId());
        return commentDtoConverter.toDto(commentRepository.save(comment));
    }

    @Override
    public CommentDto update(String id, String text) {
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Comment with id %s not found".formatted(id)));
        commentValidator.validateText(text);
        comment.setText(text);
        return commentDtoConverter.toDto(commentRepository.save(comment));
    }

}
