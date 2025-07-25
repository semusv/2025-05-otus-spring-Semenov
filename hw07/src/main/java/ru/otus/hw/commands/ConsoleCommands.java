package ru.otus.hw.commands;

import org.h2.tools.Console;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;

import java.sql.SQLException;

@SuppressWarnings({"unused"})
@ShellComponent()
public class ConsoleCommands {

    // sh2c
    @ShellMethod(value = "Start h2 console", key = "sh2c")
    public void startH2Console() {
        try {
            Console.main();
        } catch (SQLException e) {
            throw new IllegalArgumentException(e);
        }
    }
}
