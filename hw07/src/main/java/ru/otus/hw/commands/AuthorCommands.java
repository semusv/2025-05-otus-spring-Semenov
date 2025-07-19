package ru.otus.hw.commands;

import lombok.RequiredArgsConstructor;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;
import ru.otus.hw.converters.AuthorDtoConverter;
import ru.otus.hw.dto.AuthorDto;
import ru.otus.hw.services.AuthorService;

import java.util.Optional;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@ShellComponent
@SuppressWarnings({"unused"})
public class AuthorCommands {

    private final AuthorService authorService;

    private final AuthorDtoConverter authorDtoConverter;

    // aa
    @ShellMethod(value = "Find all authors", key = "aa")
    public String findAllAuthors() {
        return authorService.findAll().stream()
                .map(authorDtoConverter::authorDtoToString)
                .collect(Collectors.joining("," + System.lineSeparator()));
    }

    // abid 1
    @ShellMethod(value = "Find author by id", key = "abid")
    public String findAuthorById(@ShellOption(value = "id") long id) {
        Optional<AuthorDto> author = authorService.findById(id);
        if (author.isPresent()) {
            return authorDtoConverter.authorDtoToString(author.get());
        } else {
            return "Author with id %d not found".formatted(id);
        }
    }
}

