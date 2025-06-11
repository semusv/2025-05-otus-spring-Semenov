package ru.otus.hw.service;

import org.springframework.stereotype.Service;
import ru.otus.hw.domain.Question;

@Service
public class QuestionConverterImpl implements QuestionConverter {
    private static final String ANSWER_FORMAT = "    %d) %s";

    private static final String QUESTION_FORMAT = "%2d. %s";


    @Override
    public String convertQuestionToString(Question question, int questionNumber) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder
                .append(String.format(QUESTION_FORMAT, questionNumber, question.text()));

        int answerNumber = 1;
        for (var answer : question.answers()) {
            stringBuilder
                    .append(System.lineSeparator())
                    .append(String.format(ANSWER_FORMAT, answerNumber++, answer.text()));
        }

        return stringBuilder.toString();
    }
}
