package ru.otus.hw.service;

import ru.otus.hw.domain.ParsedLog;

@SuppressWarnings("unused")
public interface LogFilterService {
    ParsedLog filter(ParsedLog parsedLog);
}
