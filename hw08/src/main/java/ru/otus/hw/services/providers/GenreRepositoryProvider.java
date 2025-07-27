package ru.otus.hw.services.providers;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.otus.hw.exceptions.EntityNotFoundException;
import ru.otus.hw.models.Genre;
import ru.otus.hw.repositories.GenreRepository;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class GenreRepositoryProvider implements GenreProvider {
    private final GenreRepository genreRepository;

    @Override
    public List<Genre> getById(Set<String> ids) throws EntityNotFoundException {

        List<Genre> genres = genreRepository.findAllById(ids);
        Set<String> foundIds = genres.stream().map(Genre::getId).collect(Collectors.toSet());

        if (!foundIds.containsAll(ids)) {
            Set<String> missingIds = new HashSet<>(ids);
            missingIds.removeAll(foundIds);
            throw new EntityNotFoundException("Genres not found: " + missingIds);
        }

        return genres;
    }
}
