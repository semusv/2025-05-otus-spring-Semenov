package ru.otus.hw.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.otus.hw.mappers.CommentMapper;
import ru.otus.hw.dto.CommentDto;
import ru.otus.hw.exceptions.EntityNotFoundException;

import ru.otus.hw.repositories.BookRepository;
import ru.otus.hw.repositories.CommentRepository;

@RequiredArgsConstructor
@Service
public class CommentServiceImpl implements CommentService {

    private static final String COMMENT_WITH_ID_D_NOT_FOUND = "Comment with id %d not found";

    private final CommentMapper commentMapper;

    private final CommentRepository commentRepository;

    private final BookRepository bookRepository;

    @Override
    public Flux<CommentDto> findByBookId(long bookId) {
        validateBookExists(bookId);
        return commentRepository.findByBookId(bookId)
                .flatMap(comment ->
                        Mono.fromCallable(() -> commentMapper.toCommentDto(comment)));
    }

    @Override
    public Mono<Void> deleteById(long id) {
        return commentRepository.existsById(id)
                .filter(Boolean::booleanValue)
                .switchIfEmpty(Mono.error(() -> new EntityNotFoundException(
                        COMMENT_WITH_ID_D_NOT_FOUND.formatted(id),
                        "exception.entity.not.found.comment",
                        id)))
                .flatMap(exists -> commentRepository.deleteById(id));
    }

    @Override
    @Transactional
    public Mono<CommentDto> insert(CommentDto commentDto) {
        var comment = commentMapper.toComment(commentDto);
        return commentRepository.save(comment)
                .flatMap(newComment ->
                        Mono.fromCallable(() -> commentMapper.toCommentDto(newComment)));
    }


    private void validateBookExists(long id) {
        bookRepository.existsById(id)
                .switchIfEmpty(Mono.error(() ->
                        new EntityNotFoundException(
                                "Book with id %d not found".formatted(id),
                                "exception.entity.not.found.book",
                                id)
                ));
    }
}
