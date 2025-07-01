package ru.otus.hw.shell;

import lombok.RequiredArgsConstructor;
import org.springframework.shell.Availability;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellMethodAvailability;
import ru.otus.hw.commands.CommandProcessorImpl;

@ShellComponent(value = "Application Commands")
@RequiredArgsConstructor
public class ApplicationCommands {

    private final CommandProcessorImpl commandProcessor;

    @ShellMethod(value = "Start Testing", key = {"start", "s"})
    @ShellMethodAvailability("isStartCommandAvailable")
    public void start() {
        commandProcessor.start();
    }

    @ShellMethod(value = "Login User", key = {"login", "li"})
    @ShellMethodAvailability("isLogInCommandAvailable")
    public String logIn() {
        return commandProcessor.logIn();
    }

    @ShellMethod(value = "Logout User", key = {"logout", "lo"})
    @ShellMethodAvailability("isLogOutCommandAvailable")
    public String logOut() {
        return commandProcessor.logOut();
    }

    @ShellMethod(value = "Change language", key = {"language", "lng", "l"})
    public String language() {
        return commandProcessor.changeLanguage();
    }

    //Пробовал вынести в отдельный класс, но не получилось подставить правильный SpEl в ShellMethodAvailability
    public Availability isLogOutCommandAvailable() {
        return commandProcessor.isLogOutCommandAvailable();
    }

    //Пробовал вынести в отдельный класс, но не получилось подставить правильный SpEl в ShellMethodAvailability
    public Availability isStartCommandAvailable() {
        return commandProcessor.isStartCommandAvailable();
    }

    //Пробовал вынести в отдельный класс, но не получилось подставить правильный SpEl в ShellMethodAvailability
    public Availability isLogInCommandAvailable() {
        return commandProcessor.isLogInCommandAvailable();
    }


}
