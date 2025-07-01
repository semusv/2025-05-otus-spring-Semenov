package ru.otus.hw.commands;

import org.springframework.shell.Availability;

public interface CommandProcessor {
    public void start();

    public String logIn();

    public String logOut();

    public String changeLanguage();

    Availability isLogOutCommandAvailable();

    Availability isStartCommandAvailable();

    Availability isLogInCommandAvailable();
}
