package ru.otus.hw.service;

import ru.otus.hw.domain.LogAnalysisResult;
import ru.otus.hw.domain.ParsedLog;

@SuppressWarnings("unused")
public interface LogAnalysisService {
    LogAnalysisResult analyze(ParsedLog parsedLog);
}
