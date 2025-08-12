package ru.otus.hw.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.otus.hw.dto.api.BookCreateDto;
import ru.otus.hw.dto.BookDto;
import ru.otus.hw.dto.api.BookUpdateDto;
import ru.otus.hw.exceptions.EntityNotFoundException;
import ru.otus.hw.mappers.BookMapper;
import ru.otus.hw.models.Author;
import ru.otus.hw.models.Book;
import ru.otus.hw.models.Genre;
import ru.otus.hw.repositories.AuthorRepository;
import ru.otus.hw.repositories.BookRepository;
import ru.otus.hw.repositories.GenreRepository;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;


@RequiredArgsConstructor
@Service
public class BookServiceImpl implements BookService {

    private final BookRepository bookRepository;

    private final BookMapper bookMapper;

    private final AuthorRepository authorRepository;

    private final GenreRepository genreRepository;

    @Override
    @Transactional(readOnly = true)
    public BookDto findById(long id) {
        return bookRepository.findById(id)
                .map(bookMapper::toBookDto)
                .orElseThrow(() ->
                        new EntityNotFoundException(
                                "Book with id %d not found".formatted(id),
                                "exception.entity.not.found.book",
                                id)
                );
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookDto> findAll() {
        return bookRepository.findAll().stream()
                .map(bookMapper::toBookDto)
                .toList();
    }

    @Override
    @Transactional
    public void deleteById(long id) {
        bookRepository.deleteById(id);
    }

    @Override
    @Transactional
    public BookDto insert(BookCreateDto bookCreateDto) {
        var book = new Book();
        prepareBook(bookCreateDto.title(), bookCreateDto.authorId(), bookCreateDto.genreIds(), book);
        return bookMapper.toBookDto(bookRepository.save(book));
    }

    @Override
    @Transactional
    public BookDto update(BookUpdateDto bookUpdateDto) {
        Book book = bookRepository.findById(bookUpdateDto.id()).orElseThrow(
                () ->
                        new EntityNotFoundException(
                                "Book with id %d not found".formatted(bookUpdateDto.id()),
                                "exception.entity.not.found.book",
                                bookUpdateDto.id())
        );
        prepareBook(bookUpdateDto.title(), bookUpdateDto.authorId(), bookUpdateDto.genreIds(), book);
        return bookMapper.toBookDto(bookRepository.save(book));
    }

    private void prepareBook(String title, long authorId, Set<Long> genreIds, Book book) {
        book.setTitle(title);
        book.setAuthor(getAuthorById(authorId));
        book.setGenres(getGenresByIds(genreIds));
    }

    private Author getAuthorById(long id) throws EntityNotFoundException {
        return authorRepository.findById(id)
                .orElseThrow(() ->
                        new EntityNotFoundException(
                                "Author with id %d not found".formatted(id),
                                "exception.entity.not.found.author",
                                id)
                );
    }

    private List<Genre> getGenresByIds(Set<Long> ids) throws EntityNotFoundException {

        List<Genre> genres = genreRepository.findAllById(ids);
        Set<Long> foundIds = genres.stream().map(Genre::getId).collect(Collectors.toSet());

        if (!foundIds.containsAll(ids)) {
            Set<Long> missingIds = new HashSet<>(ids);
            missingIds.removeAll(foundIds);
            throw new EntityNotFoundException(
                    "Genres not found: " + missingIds,
                    "exception.entity.not.found.genres",
                    missingIds);
        }
        return genres;
    }
}
