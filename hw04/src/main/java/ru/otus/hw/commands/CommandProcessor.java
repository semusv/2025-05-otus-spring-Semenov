package ru.otus.hw.commands;

import org.springframework.shell.Availability;

public interface CommandProcessor {
    void startTest();

    String logIn();

    String logOut();

    String changeLanguage();

    Availability isLogOutCommandAvailable();

    Availability isStartCommandAvailable();

    Availability isLogInCommandAvailable();
}
