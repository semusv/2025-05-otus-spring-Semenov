package ru.otus.hw.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
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
    public Optional<BookDto> findById(String id) {
        return bookRepository.findById(id)
                .map(bookMapper::toDto);
    }

    @Override
    public List<BookDto> findAll() {
        return bookRepository.findAll()
                .stream()
                .map(bookMapper::toDto)
                .toList();
    }

    @Override
    public void deleteById(String id) {
        //Каскадное удаление через EVENT
        bookRepository.deleteById(id);
    }

    @Override
    public BookDto insert(String title, String authorId, Set<String> genresIds) {
        var book = new Book();
        prepareBook(title, authorId, genresIds, book);
        return bookMapper.toDto(bookRepository.save(book));
    }

    @Override
    public BookDto update(String id, String title, String authorId, Set<String> genresIds) {
        Book book = bookRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException("Book with id %s not found".formatted(id))
        );
        prepareBook(title, authorId, genresIds, book);
        return bookMapper.toDto(bookRepository.save(book));
    }

    private void prepareBook(String title, String authorId, Set<String> genresIds, Book book) {
        bookValidator.validateTitle(title);
        book.setTitle(title);
        book.setAuthor(getAuthorById(authorId));
        book.setGenres(getGenreById(genresIds));
        bookValidator.validateGenres(book.getGenres());
    }

    private Author getAuthorById(String id) throws EntityNotFoundException {
        return authorRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Author with id %s not found".formatted(id)));
    }

    private List<Genre> getGenreById(Set<String> ids) throws EntityNotFoundException {

        List<Genre> genres = genreRepository.findAllById(ids);
        Set<String> foundIds = genres.stream().map(Genre::getId).collect(Collectors.toSet());

        if (!foundIds.containsAll(ids)) {
            Set<String> missingIds = new HashSet<>(ids);
            missingIds.removeAll(foundIds);
            throw new EntityNotFoundException("Genres not found: " + missingIds);
        }

        return genres;
    }
}
