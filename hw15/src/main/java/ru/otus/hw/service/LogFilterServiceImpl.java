package ru.otus.hw.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.otus.hw.domain.LogLevel;
import ru.otus.hw.domain.ParsedLog;

@Slf4j
@Service
public class LogFilterServiceImpl implements LogFilterService {

    @Override
    public ParsedLog filter(ParsedLog parsedLog) {
        // Фильтруем только ERROR и CRITICAL логи
        if (parsedLog.level() == LogLevel.ERROR || parsedLog.level() == LogLevel.CRITICAL) {
            log.info("Passing through filter: {}", parsedLog.message());
            return parsedLog;
        }
        log.debug("Filtered out: {}", parsedLog.message());
        return null; // Будет отфильтровано
    }
}
