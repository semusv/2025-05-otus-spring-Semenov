package ru.otus.hw.commands;

import lombok.RequiredArgsConstructor;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;
import ru.otus.hw.converters.AuthorConverter;
import ru.otus.hw.models.Author;
import ru.otus.hw.services.AuthorService;

import java.util.Optional;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@ShellComponent
public class AuthorCommands {

    private final AuthorService authorService;

    private final AuthorConverter authorConverter;

    @ShellMethod(value = "Find all authors", key = "aa")
    public String findAllAuthors() {
        return authorService.findAll().stream()
                .map(authorConverter::authorToString)
                .collect(Collectors.joining("," + System.lineSeparator()));
    }

    // abid 1
    @ShellMethod(value = "Find author by id", key = "abid")
    public String findAuthorById(@ShellOption(value = "id") long id) {
        Optional<Author> author = authorService.findById(id);
        if (author.isPresent()) {
            return authorConverter.authorToString(author.get());
        } else {
            return "Author with id %d not found".formatted(id);
        }
    }
}

