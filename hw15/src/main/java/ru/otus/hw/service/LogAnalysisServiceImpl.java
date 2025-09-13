package ru.otus.hw.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.otus.hw.domain.LogAnalysisResult;
import ru.otus.hw.domain.LogLevel;
import ru.otus.hw.domain.ParsedLog;

@Slf4j
@Service
public class LogAnalysisServiceImpl implements LogAnalysisService {

    @Override
    public LogAnalysisResult analyze(ParsedLog parsedLog) {
        log.info("Analyzing log: {}", parsedLog.message());

        boolean needsNotification = parsedLog.level() == LogLevel.CRITICAL ||
                                    parsedLog.message().contains("exception") ||
                                    parsedLog.message().contains("error");

        String pattern = detectPattern(parsedLog.message());

        return new LogAnalysisResult(
                parsedLog.component(),
                parsedLog.level(),
                pattern,
                needsNotification
        );
    }

    private String detectPattern(String message) {
        if (message.contains("timeout")) {
            return "TIMEOUT";
        }
        if (message.contains("memory")) {
            return "MEMORY_ISSUE";
        }
        if (message.contains("database")) {
            return "DB_ISSUE";
        }
        return "GENERIC";
    }
}