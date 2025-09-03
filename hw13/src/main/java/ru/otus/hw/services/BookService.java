package ru.otus.hw.services;

import ru.otus.hw.dto.api.BookFormDto;
import ru.otus.hw.dto.BookDto;

import java.util.List;


public interface BookService {
    BookDto findById(long id);

    List<BookDto> findAll();

    BookDto insert(BookFormDto bookCreateDto);

    BookDto update(Long id, BookFormDto bookFormDto);

    void deleteById(long id);
}
