package ru.otus.hw.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.otus.hw.mappers.BookMapper;
import ru.otus.hw.dto.BookDto;
import ru.otus.hw.exceptions.EntityNotFoundException;
import ru.otus.hw.models.Author;
import ru.otus.hw.models.Book;
import ru.otus.hw.models.Genre;
import ru.otus.hw.repositories.AuthorRepository;
import ru.otus.hw.repositories.BookRepository;
import ru.otus.hw.repositories.GenreRepository;
import ru.otus.hw.services.validators.BookValidator;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;


@RequiredArgsConstructor
@Service
public class BookServiceImpl implements BookService {

    private final BookRepository bookRepository;

    private final BookMapper bookMapper;

    private final AuthorRepository authorRepository;

    private final GenreRepository genreRepository;


    private final BookValidator bookValidator;

    @Override
    @Transactional(readOnly = true)
    public Optional<BookDto> findById(long id) {
        return bookRepository.findById(id).map(bookMapper::bookToBookFullDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookDto> findAll() {
        return bookRepository.findAll().stream()
                .map(bookMapper::bookToBookFullDto)
                .toList();
    }

    @Override
    @Transactional
    public void deleteById(long id) {
        bookRepository.deleteById(id);
    }

    @Override
    @Transactional
    public BookDto insert(String title, long authorId, Set<Long> genresIds) {
        var book = new Book();
        prepareBook(title, authorId, genresIds, book);
        return bookMapper.bookToBookFullDto(bookRepository.save(book));
    }

    @Override
    @Transactional
    public BookDto update(long id, String title, long authorId, Set<Long> genresIds) {
        Book book = bookRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException("Book with id %d not found".formatted(id))
        );
        prepareBook(title, authorId, genresIds, book);
        return bookMapper.bookToBookFullDto(bookRepository.save(book));
    }

    private void prepareBook(String title, long authorId, Set<Long> genresIds, Book book) {
        book.setTitle(title);
        bookValidator.validateTitle(book.getTitle());
        book.setAuthor(fetchAuthor(authorId));
        book.setGenres(fetchGenres(genresIds));
        bookValidator.validateGenres(book.getGenres());
    }

    private Author fetchAuthor(long id) throws EntityNotFoundException {
        return authorRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Author with id %d not found".formatted(id)));
    }

    private List<Genre> fetchGenres(Set<Long> ids) throws EntityNotFoundException {

        List<Genre> genres = genreRepository.findAllById(ids);
        Set<Long> foundIds = genres.stream().map(Genre::getId).collect(Collectors.toSet());

        if (!foundIds.containsAll(ids)) {
            Set<Long> missingIds = new HashSet<>(ids);
            missingIds.removeAll(foundIds);
            throw new EntityNotFoundException("Genres not found: " + missingIds);
        }
        return genres;
    }
}
