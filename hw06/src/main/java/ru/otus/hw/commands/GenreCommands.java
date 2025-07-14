package ru.otus.hw.commands;

import lombok.RequiredArgsConstructor;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;
import ru.otus.hw.converters.GenreConverter;
import ru.otus.hw.services.GenreService;

import java.util.Set;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@ShellComponent
public class GenreCommands {

    private final GenreService genreService;

    private final GenreConverter genreConverter;

    @ShellMethod(value = "Find all genres", key = "ag")
    public String findAllGenres() {
        return genreService.findAll().stream()
                .map(genreConverter::genreToString)
                .collect(Collectors.joining("," + System.lineSeparator()));
    }

    @ShellMethod(value = "Find genres by ids", key = "gbids")
    public String findGenresByIds(@ShellOption(value = "ids") Set<Long> ids) {
        if (ids.isEmpty()) {
            return "Please provide at least one id";
        } else {
            return genreService.findAllByIds(ids).stream()
                    .map(genreConverter::genreToString)
                    .collect(Collectors.joining("," + System.lineSeparator()));
        }
    }
}
