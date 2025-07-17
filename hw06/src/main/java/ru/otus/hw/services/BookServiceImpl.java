package ru.otus.hw.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.otus.hw.converters.BookFullDtoConverter;
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

    private final BookFullDtoConverter bookFullDtoConverter;

    private final AuthorProvider authorProvider;

    private final GenreProvider genreProvider;

    private final BookValidator bookValidator;

    @Override
    @Transactional(readOnly = true)
    public Optional<BookDto> findById(long id) {
        return bookRepository.findById(id).map(bookFullDtoConverter::bookToBookFullDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookDto> findAll() {
        return bookRepository.findAll().stream()
                .map(bookFullDtoConverter::bookToBookFullDto)
                .toList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteById(long id) {
        bookRepository.deleteById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BookDto insert(String title, long authorId, Set<Long> genresIds) {
        var book = new Book();
        prepareBook(title, authorId, genresIds, book);
        return bookFullDtoConverter.bookToBookFullDto(bookRepository.save(book));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BookDto update(long id, String title, long authorId, Set<Long> genresIds) {
        Book book = bookRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException("Book with id %d not found".formatted(id))
        );
        prepareBook(title, authorId, genresIds, book);
        return bookFullDtoConverter.bookToBookFullDto(bookRepository.save(book));
    }

    private void prepareBook(String title, long authorId, Set<Long> genresIds, Book book) {
        book.setTitle(title);
        bookValidator.validateTitle(book.getTitle());
        book.setAuthor(authorProvider.fetchAuthor(authorId));
        book.setGenres(genreProvider.fetchGenres(genresIds));
        bookValidator.validateGenres(book.getGenres());
    }
}
