package ru.otus.hw.converters;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.otus.hw.dto.BookDto;
import ru.otus.hw.models.Book;

import java.util.stream.Collectors;

@RequiredArgsConstructor
@Component
public class BookFullDtoConverter {
    private final AuthorDtoConverter authorDtoConverter;

    private final GenreDtoConverter genreDtoConverter;


    public String bookFullDtoToString(BookDto book) {
        var genresString = book.genres().stream()
                .map(genreDtoConverter::genreDtoToString)
                .map("{%s}"::formatted)
                .collect(Collectors.joining(", "));

        return "Id: %d, title: %s, author: {%s}, genres: [%s]".formatted(
                book.id(),
                book.title(),
                authorDtoConverter.authorDtoToString(book.author()),
                genresString);
    }

    public BookDto bookToBookFullDto(Book book) {
        if (book != null) {
            return new BookDto(
                    book.getId(),
                    book.getTitle(),
                    authorDtoConverter.authorToDto(book.getAuthor()),
                    book.getGenres().stream().map(genreDtoConverter::genreToDto).toList()
            );
        }
        return null;
    }
}
