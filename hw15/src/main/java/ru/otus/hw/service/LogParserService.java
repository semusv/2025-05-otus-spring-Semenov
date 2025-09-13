package ru.otus.hw.service;

import ru.otus.hw.domain.ParsedLog;
import ru.otus.hw.domain.RawLog;

@SuppressWarnings("unused")
public interface LogParserService {
    ParsedLog parse(RawLog rawLog);
}
