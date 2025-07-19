package ru.otus.hw.services.providers;


import jakarta.validation.ValidationException;
import ru.otus.hw.exceptions.EntityNotFoundException;
import ru.otus.hw.models.Book;

public interface BookProvider {
    Book fetchBook(long id) throws EntityNotFoundException;

    void validateBookExists(long id) throws ValidationException;
}
