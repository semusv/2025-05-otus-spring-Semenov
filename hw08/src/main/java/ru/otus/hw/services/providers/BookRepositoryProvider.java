package ru.otus.hw.services.providers;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.otus.hw.exceptions.EntityNotFoundException;
import ru.otus.hw.models.Book;
import ru.otus.hw.repositories.BookRepository;

@Component
@RequiredArgsConstructor
public class BookRepositoryProvider implements BookProvider {

    private final BookRepository bookRepository;

    @Override
    public Book fetchBook(String id) throws EntityNotFoundException {
        return bookRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Book with id %s not found".formatted(id)));
    }

    @Override
    public void validateBookExists(String id) throws EntityNotFoundException {
        if (!bookRepository.existsById(id)) {
            throw new EntityNotFoundException("Book with id %s not found".formatted(id));
        }
    }

}
