package ru.otus.hw.converters;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.otus.hw.dto.BookFullDto;
import ru.otus.hw.models.Book;

import java.util.stream.Collectors;

@RequiredArgsConstructor
@Component
public class BookFullDtoConverter {
    private final AuthorDtoConverter authorDtoConverter;

    private final GenreDtoConverter genreDtoConverter;

    private final CommentDtoConverter commentDtoConverter;

    public String bookFullDtoToString(BookFullDto book) {
        var genresString = book.genres().stream()
                .map(genreDtoConverter::genreDtoToString)
                .map("{%s}"::formatted)
                .collect(Collectors.joining(", "));

        var commentString = book.comments().stream()
                .map(commentDtoConverter::commentDtoToString)
                .map("{%s}"::formatted)
                .collect(Collectors.joining(", "));

        return "Id: %d, title: %s, author: {%s}, genres: [%s], comments: [%s]".formatted(
                book.id(),
                book.title(),
                authorDtoConverter.authorDtoToString(book.author()),
                genresString,
                commentString);
    }

    public BookFullDto bookToBookFullDto(Book book) {
        if (book != null) {
            return new BookFullDto(
                    book.getId(),
                    book.getTitle(),
                    authorDtoConverter.authorToDto(book.getAuthor()),
                    book.getGenres().stream().map(genreDtoConverter::genreToDto).toList(),
                    book.getComments().stream().map(commentDtoConverter::commentToDto).toList()
            );
        }
        return null;
    }
}
