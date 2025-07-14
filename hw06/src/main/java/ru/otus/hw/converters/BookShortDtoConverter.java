package ru.otus.hw.converters;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.otus.hw.dto.BookShortDto;
import ru.otus.hw.models.Book;

@RequiredArgsConstructor
@Component
public class BookShortDtoConverter {

    private final AuthorDtoConverter authorDtoConverter;

    public String bookShortDtoToString(BookShortDto book) {

        return "Id: %d, title: %s, author: %s ".formatted(
                book.id(),
                book.title(),
                book.author().fullName());
    }

    public BookShortDto bookToBookShortDto(Book book) {
        if (book != null) {
            return new BookShortDto(
                    book.getId(),
                    book.getTitle(),
                    authorDtoConverter.authorToDto(book.getAuthor())
            );
        }
        return null;
    }
}
