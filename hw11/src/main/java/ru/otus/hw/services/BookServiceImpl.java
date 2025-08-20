package ru.otus.hw.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.otus.hw.dto.api.BookCreateDto;
import ru.otus.hw.dto.BookDto;
import ru.otus.hw.dto.api.BookUpdateDto;
import ru.otus.hw.exceptions.EntityNotFoundException;
import ru.otus.hw.mappers.BookMapper;
import ru.otus.hw.models.Book;
import ru.otus.hw.repositories.AuthorRepository;
import ru.otus.hw.repositories.BookRepository;
import ru.otus.hw.repositories.GenreRepository;

import java.util.Set;


@RequiredArgsConstructor
@Service
public class BookServiceImpl implements BookService {

    private final BookRepository bookRepository;

    private final BookMapper bookMapper;

    private final AuthorRepository authorRepository;

    private final GenreRepository genreRepository;

    @Override
    public Mono<BookDto> findById(long id) {
        return bookRepository.findById(id)
                .switchIfEmpty(Mono.error(() ->
                        new EntityNotFoundException(
                                "Book with id %d not found".formatted(id),
                                "exception.entity.not.found.book",
                                id)
                ))
                .map(bookMapper::toBookDto);
    }

    @Override
    public Flux<BookDto> findAll() {
        return bookRepository.findAll()
                .flatMap(book ->
                        Mono.fromCallable(() -> bookMapper.toBookDto(book)));
    }

    @Override
    @Transactional
    public Mono<Void> deleteById(long id) {
       return bookRepository.deleteById(id);
    }

    @Override
    @Transactional
    public Mono<BookDto> insert(BookCreateDto bookCreateDto) {
        return validateGenresExists(bookCreateDto.genreIds())
                .then(validateAuthorExists(bookCreateDto.authorId()))
                .then(Mono.defer(() -> {
                    Book book = bookMapper.toBook(bookCreateDto);
                    return bookRepository.saveBookWithGenres(book);
                }))
                .flatMap(newBook -> Mono.fromCallable(() -> bookMapper.toBookDto(newBook)));
    }

    private Mono<Void> validateAuthorExists(long id) {
        return authorRepository.existsById(id)
                .switchIfEmpty(Mono.error(() ->
                        new EntityNotFoundException(
                                "Book with id %d not found".formatted(id),
                                "exception.entity.not.found.book",
                                id)
                ))
                .then();
    }

    private Mono<Void> validateGenresExists(Set<Long> genresIds) {
        return genreRepository.countByIdIn(genresIds)
                .filter(count -> count == genresIds.size())
                .switchIfEmpty(Mono.error(
                        new EntityNotFoundException(
                                "Not all genres in DB %s".formatted(genresIds),
                                "exception.entity.not.found.genres")))
                .then();
    }

    @Override
    @Transactional
    public Mono<BookDto> update(BookUpdateDto bookUpdateDto) {
        return validateGenresExists(bookUpdateDto.genreIds())
                .then(validateAuthorExists(bookUpdateDto.authorId()))
                .then(validateGenresExists(bookUpdateDto.genreIds()))
                .then(Mono.defer(() -> {
                    Book book = bookMapper.toBook(bookUpdateDto);
                    return bookRepository.saveBookWithGenres(book);
                }))
                .flatMap(newBook -> Mono.fromCallable(() -> bookMapper.toBookDto(newBook)));
    }
}
