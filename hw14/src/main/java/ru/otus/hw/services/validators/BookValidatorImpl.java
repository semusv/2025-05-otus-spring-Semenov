package ru.otus.hw.services.validators;

import jakarta.validation.ValidationException;
import org.springframework.stereotype.Component;
import ru.otus.hw.models.h2.Genre;

import java.util.List;

@Component
public class BookValidatorImpl implements BookValidator {
    @Override
    public void validateTitle(String title) throws ValidationException {
        if (title == null || title.trim().isEmpty()) {
            throw new ValidationException("Title cannot be empty");
        }
    }

    @Override
    public void validateGenres(List<Genre> genres) throws ValidationException {
        if (genres == null || genres.isEmpty()) {
            throw new ValidationException("Genres cannot be empty");
        }
    }
}
