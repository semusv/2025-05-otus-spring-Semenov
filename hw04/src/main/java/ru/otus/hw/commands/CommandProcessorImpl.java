package ru.otus.hw.commands;


import lombok.RequiredArgsConstructor;
import org.springframework.shell.Availability;
import org.springframework.stereotype.Component;
import ru.otus.hw.config.AppProperties;
import ru.otus.hw.service.LocalizedIOService;
import ru.otus.hw.service.StudentService;
import ru.otus.hw.service.TestRunnerService;

import java.util.Locale;

@Component
@RequiredArgsConstructor
public class CommandProcessorImpl implements CommandProcessor {

    private final TestRunnerService testRunnerService;

    private final StudentService studentService;

    private final LocalizedIOService localizedIOService;

    private final AppProperties appProperties;

    @Override
    public void startTest() {
        testRunnerService.run();
    }

    @Override
    public String logIn() {
        studentService.logIn();
        return localizedIOService.getMessage("CommandProcessorImpl.log.in.success");
    }

    @Override
    public String logOut() {
        studentService.logOut();
        return localizedIOService.getMessage("CommandProcessorImpl.log.out.success");
    }

    @Override
    public String changeLanguage() {
        String localeString = localizedIOService
                .readStringWithPromptLocalized("CommandProcessorImpl.changeLanguage.prompt");

        if (localeString == null || localeString.trim().isEmpty()) {
            return localizedIOService.getMessage("CommandProcessorImpl.changeLanguage.error");
        }

        localeString = localeString.replace("_", "-");
        if (matchLocale(localeString)) {
            appProperties.setLocale(localeString);
            return localizedIOService.getMessage("CommandProcessorImpl.changeLanguage.success", localeString);
        } else {
            return localizedIOService.getMessage("CommandProcessorImpl.changeLanguage.error");
        }
    }

    @Override
    public Availability isLogOutCommandAvailable() {
        return studentService.getCurrentStudent() != null
                ? Availability.available()
                : Availability.unavailable(localizedIOService.getMessage("ApplicationCommands.not.authorized"));
    }

    @Override
    public Availability isStartCommandAvailable() {
        return studentService.getCurrentStudent() != null
                ? Availability.available()
                : Availability.unavailable(localizedIOService.getMessage("ApplicationCommands.login.advice"));
    }

    @Override
    public Availability isLogInCommandAvailable() {
        return studentService.getCurrentStudent() == null
                ? Availability.available()
                : Availability.unavailable(localizedIOService.getMessage("ApplicationCommands.logout.advice"));
    }


    private boolean matchLocale(String localeString) {
        try {
            // Поддержка форматов en-US, en_US, en
            Locale locale = Locale.forLanguageTag(localeString);
            return !locale.getLanguage().isEmpty();
        } catch (Exception e) {
            return false;
        }
    }
}
