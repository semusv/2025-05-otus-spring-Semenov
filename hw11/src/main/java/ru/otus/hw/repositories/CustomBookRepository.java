package ru.otus.hw.repositories;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.otus.hw.models.Book;

public interface CustomBookRepository {
    Flux<Book> findAll();

    Mono<Book> findById(long id);

    Mono<Book> saveBookWithGenres(Book book);
}
