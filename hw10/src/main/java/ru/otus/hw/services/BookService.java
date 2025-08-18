package ru.otus.hw.services;

import ru.otus.hw.dto.api.BookCreateDto;
import ru.otus.hw.dto.BookDto;
import ru.otus.hw.dto.api.BookUpdateDto;

import java.util.List;


public interface BookService {
    BookDto findById(long id);

    List<BookDto> findAll();

    BookDto insert(BookCreateDto bookCreateDto);

    BookDto update(BookUpdateDto bookUpdateDto);

    void deleteById(long id);
}
