package ru.otus.hw.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.otus.hw.domain.LogLevel;
import ru.otus.hw.domain.ParsedLog;
import ru.otus.hw.domain.RawLog;

@Slf4j
@Service
public class LogParserServiceImpl implements LogParserService {

    @Override
    public ParsedLog parse(RawLog rawLog) {
        log.info("Parsing log: {}", rawLog.logLine());
        // Простой парсинг
        String[] parts = rawLog.logLine().split(" ", 4);
        LogLevel level = extractLogLevel(parts);
        String component = parts.length > 1 ? parts[1] : "unknown";
        String message = parts.length > 3 ? parts[3] : rawLog.logLine();

        return new ParsedLog(message, level, component, rawLog.timestamp());
    }

    private LogLevel extractLogLevel(String[] parts) {
        if (parts.length > 0) {
            String levelStr = parts[0].toUpperCase();
            try {
                return LogLevel.valueOf(levelStr);
            } catch (IllegalArgumentException e) {
                return LogLevel.INFO;
            }
        }
        return LogLevel.INFO;
    }
}
