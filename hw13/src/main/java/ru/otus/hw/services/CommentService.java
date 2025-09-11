package ru.otus.hw.services;

import ru.otus.hw.dto.CommentDto;

import java.util.List;


public interface CommentService {

    CommentDto findById(long id);

    List<CommentDto> findByBookId(long bookId);

    CommentDto insert(CommentDto commentDto);

    CommentDto update(CommentDto commentDto);

    void deleteById(long id);
}
