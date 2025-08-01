package ru.otus.hw.services.providers;


import ru.otus.hw.exceptions.EntityNotFoundException;
import ru.otus.hw.models.Author;

public interface AuthorProvider {
    Author fetchAuthor(long id) throws EntityNotFoundException;
}
