package ru.otus.hw.service;

import lombok.RequiredArgsConstructor;
import ru.otus.hw.dao.QuestionDao;
import ru.otus.hw.domain.Question;

import java.util.List;

@RequiredArgsConstructor
public class TestServiceImpl implements TestService {

    private static final String ANSWER_SPACES = "    ";

    private final IOService ioService;

    private final QuestionDao questionDao;

    @Override
    public void executeTest() {
        ioService.printLine("");
        ioService.printFormattedLine("Please answer the questions below%n");

        printFullQuestions(questionDao.findAll());
    }

    private void printFullQuestions(List<Question> questionList) {
        int questionNumber = 0;

        if (questionList == null || questionList.isEmpty()) {
            throw new RuntimeException("No questions found");
        }

        for (var q : questionList) {
            printOneQuestionAndAnswers(q, ++questionNumber);
        }
    }

    private void printOneQuestionAndAnswers(Question q, int questionNumber) {
        int answerNumber = 0;
        this.printQuestion(q.text(), questionNumber);
        for (var a : q.answers()) {
            this.printAnswer(a.text(), answerNumber++);
        }
        ioService.printLine("");
    }

    private void printAnswer(String text, int i) {
        char letter = (char) ('a' + i);
        ioService.printFormattedLine(ANSWER_SPACES + "%c) %s", letter, text);
    }

    private void printQuestion(String text, int questionNumber) {
        ioService.printFormattedLine("%2d. %s", questionNumber, text);
    }
}
