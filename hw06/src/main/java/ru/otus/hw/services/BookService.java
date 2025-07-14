package ru.otus.hw.services;

import ru.otus.hw.dto.BookFullDto;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface BookService {
    Optional<BookFullDto> findById(long id);

    List<BookFullDto> findAll();

    BookFullDto insert(String title, long authorId, Set<Long> genresIds);

    BookFullDto update(long id, String title, long authorId, Set<Long> genresIds);

    void deleteById(long id);
}
