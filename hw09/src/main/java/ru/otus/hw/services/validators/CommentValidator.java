package ru.otus.hw.services.validators;

import jakarta.validation.ValidationException;

public interface CommentValidator {
    void validateText(String text) throws ValidationException;
}
