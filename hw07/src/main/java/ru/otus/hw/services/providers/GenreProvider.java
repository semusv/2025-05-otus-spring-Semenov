package ru.otus.hw.services.providers;


import ru.otus.hw.exceptions.EntityNotFoundException;
import ru.otus.hw.models.Genre;

import java.util.List;
import java.util.Set;

public interface GenreProvider {
    List<Genre> fetchGenres(Set<Long> ids) throws EntityNotFoundException;
}
