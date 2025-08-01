package ru.otus.hw.services;

import ru.otus.hw.dto.BookDto;
import ru.otus.hw.dto.BookFormDto;
import java.util.List;


public interface BookService {
    BookDto findById(long id);

    List<BookDto> findAll();

    BookDto insert(BookFormDto bookFormDto);

    BookDto update(BookFormDto bookFormDto);

    void deleteById(long id);
}
