package ru.otus.hw.commands;

import org.springframework.shell.Availability;

public interface CommandProcessor {
    void start();

    String logIn();

    String logOut();

    String changeLanguage();

    Availability isLogOutCommandAvailable();

    Availability isStartCommandAvailable();

    Availability isLogInCommandAvailable();
}
