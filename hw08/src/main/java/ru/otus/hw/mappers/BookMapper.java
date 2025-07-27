package ru.otus.hw.mappers;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.otus.hw.dto.BookDto;
import ru.otus.hw.models.Book;

import java.util.stream.Collectors;

@RequiredArgsConstructor
@Component
public class BookMapper {
    private final AuthorMapper authorMapper;

    private final GenreMapper genreDtoConverter;


    public String dtoToString(BookDto book) {
        var genresString = book.genres().stream()
                .map(genreDtoConverter::dtoToString)
                .map("{%s}"::formatted)
                .collect(Collectors.joining(", "));

        return "Id: %s, title: %s, author: {%s}, genres: [%s]".formatted(
                book.id(),
                book.title(),
                authorMapper.dtoToString(book.author()),
                genresString);
    }

    public BookDto toDto(Book book) {
        if (book != null) {
            return new BookDto(
                    book.getId(),
                    book.getTitle(),
                    authorMapper.toDto(book.getAuthor()),
                    book.getGenres().stream().map(genreDtoConverter::toDto).toList()
            );
        }
        return null;
    }
}
