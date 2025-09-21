package ru.otus.hw.services.validators;

import jakarta.validation.ValidationException;
import ru.otus.hw.models.h2.Genre;

import java.util.List;

public interface BookValidator {
    void validateTitle(String title) throws ValidationException;

    void validateGenres(List<Genre> genres) throws ValidationException;
}
