package ru.otus.hw.commands;

import lombok.RequiredArgsConstructor;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;
import ru.otus.hw.mappers.BookMapper;
import ru.otus.hw.services.BookService;

import java.util.Set;
import java.util.stream.Collectors;

@SuppressWarnings({"SpellCheckingInspection", "unused"})
@RequiredArgsConstructor
@ShellComponent
public class BookCommands {

    private final BookService bookService;

    private final BookMapper bookMapper;

    //ab
    @ShellMethod(value = "Find all books", key = "ab")
    public String findAllBooks() {
        return bookService.findAll().stream()
                .map(bookMapper::dtoToString)
                .collect(Collectors.joining("," + System.lineSeparator()));
    }

    // bbid 1
    @ShellMethod(value = "Find book by id", key = "bbid")
    public String findBookById(@ShellOption(value = "id") String id) {
        return bookService.findById(id)
                .map(bookMapper::dtoToString)
                .orElse("Book with id %s not found".formatted(id));
    }

    // bins newBook 1 1,6
    @ShellMethod(value = "Insert book", key = "bins")
    public String insertBook(@ShellOption(value = "title") String title,
                             @ShellOption(value = "aid") String authorId,
                             @ShellOption(value = "gids") Set<String> genresIds) {
        var savedBook = bookService.insert(title, authorId, genresIds);
        return bookMapper.dtoToString(savedBook);
    }

    // bupd 4 editedBook 3 2,5
    @ShellMethod(value = "Update book", key = "bupd")
    public String updateBook(@ShellOption(value = "id") String id,
                             @ShellOption(value = "title") String title,
                             @ShellOption(value = "aid") String authorId,
                             @ShellOption(value = "gids") Set<String> genresIds) {
        var savedBook = bookService.update(id, title, authorId, genresIds);
        return bookMapper.dtoToString(savedBook);
    }

    // bdel 4
    @ShellMethod(value = "Delete book by id", key = "bdel")
    public void deleteBook(@ShellOption(value = "id") String id) {
        bookService.deleteById(id);
    }
}
