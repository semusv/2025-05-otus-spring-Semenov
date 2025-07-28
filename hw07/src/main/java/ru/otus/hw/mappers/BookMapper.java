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


    public String bookFullDtoToString(BookDto book) {
        var genresString = book.genres().stream()
                .map(genreDtoConverter::genreDtoToString)
                .map("{%s}"::formatted)
                .collect(Collectors.joining(", "));

        return "Id: %d, title: %s, author: {%s}, genres: [%s]".formatted(
                book.id(),
                book.title(),
                authorMapper.authorDtoToString(book.author()),
                genresString);
    }

    public BookDto bookToBookFullDto(Book book) {
        if (book != null) {
            return new BookDto(
                    book.getId(),
                    book.getTitle(),
                    authorMapper.authorToDto(book.getAuthor()),
                    book.getGenres().stream().map(genreDtoConverter::genreToDto).toList()
            );
        }
        return null;
    }
}
