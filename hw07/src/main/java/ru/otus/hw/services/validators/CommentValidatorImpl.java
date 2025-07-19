package ru.otus.hw.services.validators;

import jakarta.validation.ValidationException;
import org.springframework.stereotype.Component;

@Component
public class CommentValidatorImpl implements CommentValidator {
    @Override
    public void validateText(String text) throws ValidationException {
        if (text == null || text.trim().isEmpty()) {
            throw new ValidationException("Text cannot be empty");
        }
    }
}
