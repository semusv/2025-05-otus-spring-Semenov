package ru.otus.hw.service;

import ru.otus.hw.domain.Question;

public interface QuestionConverter {
    String convertQuestionToString(Question question, int questionNumber);
}
