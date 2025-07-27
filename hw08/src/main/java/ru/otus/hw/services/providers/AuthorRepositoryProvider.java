package ru.otus.hw.services.providers;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.otus.hw.exceptions.EntityNotFoundException;
import ru.otus.hw.models.Author;
import ru.otus.hw.repositories.AuthorRepository;

@Component
@RequiredArgsConstructor
public class AuthorRepositoryProvider implements AuthorProvider {

    private final AuthorRepository authorRepository;

    @Override
    public Author getById(String id) throws EntityNotFoundException {
        return authorRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Author with id %s not found".formatted(id)));
    }
}
