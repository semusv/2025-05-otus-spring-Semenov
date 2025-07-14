package ru.otus.hw.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.otus.hw.converters.BookFullDtoConverter;
import ru.otus.hw.dto.BookFullDto;
import ru.otus.hw.exceptions.EntityNotFoundException;
import ru.otus.hw.models.Book;
import ru.otus.hw.models.Comment;
import ru.otus.hw.repositories.AuthorRepository;
import ru.otus.hw.repositories.BookRepository;
import ru.otus.hw.repositories.CommentRepository;
import ru.otus.hw.repositories.GenreRepository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.springframework.util.CollectionUtils.isEmpty;

@RequiredArgsConstructor
@Service
public class BookServiceImpl implements BookService {
    private final AuthorRepository authorRepository;

    private final GenreRepository genreRepository;

    private final BookRepository bookRepository;

    private final BookFullDtoConverter bookFullDtoConverter;

    private final CommentRepository commentRepository;

    @Override
    @Transactional(readOnly = true)
    public Optional<BookFullDto> findById(long id) {
        return bookRepository.findById(id).map(bookFullDtoConverter::bookToBookFullDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookFullDto> findAll() {
        return bookRepository.findAll().stream()
                .map(bookFullDtoConverter::bookToBookFullDto)
                .toList();
    }

    @Override
    @Transactional
    public void deleteById(long id) {
        bookRepository.deleteById(id);
    }

    @Override
    @Transactional
    public BookFullDto insert(String title, long authorId, Set<Long> genresIds) {
        return save(0, title, authorId, genresIds);
    }

    @Override
    @Transactional
    public BookFullDto update(long id, String title, long authorId, Set<Long> genresIds) {
        if (bookRepository.notExistsById(id)) {
            throw  new EntityNotFoundException("Book with id %d not found".formatted(id));
        }
        return save(id, title, authorId, genresIds);
    }


    private BookFullDto save(long id, String title, long authorId, Set<Long> genresIds) {
        if (isEmpty(genresIds)) {
            throw new IllegalArgumentException("Genres ids must not be null");
        }
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("Title must not be empty");
        }
        var author = authorRepository.findById(authorId)
                .orElseThrow(() -> new EntityNotFoundException("Author with id %d not found".formatted(authorId)));
        var genres = genreRepository.findAllByIds(genresIds);
        if (isEmpty(genres) || genresIds.size() != genres.size()) {
            throw new EntityNotFoundException("One or all genres with ids %s not found".formatted(genresIds));
        }
        List<Comment> comments = commentRepository.findByBookId(id);
        var book = new Book(id, title, author, genres, comments);
        return bookFullDtoConverter.bookToBookFullDto(bookRepository.save(book));
    }
}
