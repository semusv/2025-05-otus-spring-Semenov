package ru.otus.hw.services;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.otus.hw.dto.CommentDto;


public interface CommentService {

    Flux<CommentDto> findByBookId(long bookId);

    Mono<CommentDto> insert(CommentDto commentDto);

    Mono<Void> deleteById(long id);
}
