package ru.otus.hw.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.otus.hw.mappers.BookMapper;
import ru.otus.hw.dto.BookDto;
import ru.otus.hw.exceptions.EntityNotFoundException;
import ru.otus.hw.models.Book;
import ru.otus.hw.repositories.BookRepository;
import ru.otus.hw.services.providers.AuthorProvider;
import ru.otus.hw.services.validators.BookValidator;
import ru.otus.hw.services.providers.GenreProvider;

import java.util.List;
import java.util.Optional;
import java.util.Set;


@RequiredArgsConstructor
@Service
public class BookServiceImpl implements BookService {

    private final BookRepository bookRepository;

    private final BookMapper bookMapper;

    private final AuthorProvider authorProvider;

    private final GenreProvider genreProvider;

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
        if (!bookRepository.existsById(id)) {
            throw new EntityNotFoundException("Book with id %s not found".formatted(id));
        }
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
        book.setAuthor(authorProvider.getById(authorId));
        book.setGenres(genreProvider.getById(genresIds));
        bookValidator.validateGenres(book.getGenres());
    }
}
