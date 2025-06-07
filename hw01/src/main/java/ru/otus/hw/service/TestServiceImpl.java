package ru.otus.hw.service;

import lombok.RequiredArgsConstructor;
import ru.otus.hw.dao.QuestionDao;
import ru.otus.hw.domain.Question;

import java.util.List;

@RequiredArgsConstructor
public class TestServiceImpl implements TestService {
    //Заголовок теста
    private static final String TOPIC_LINE = "%nPlease answer the questions below%n";

    //Вопросов не было найдено
    private static final String NO_QUESTIONS_FOUND = "No questions found";

    //Формат вывода ответа
    private static final String ANSWER_FORMAT = "    %c) %s";

    //Формат вывода вопроса
    private static final String QUESTION_FORMAT = "%2d. %s";

    //Начальный символ для ответов;
    private static final char START_CHAR_FOR_ANSWER = 'a';

    private final IOService ioService;

    private final QuestionDao questionDao;

    @Override
    public void executeTest() {
        ioService.printFormattedLine(TOPIC_LINE);
        printFullQuestions(questionDao.findAll());
    }

    /**
     * Выводит все вопросы и ответы из списка.
     *
     * @param questionList список вопросов
     * @throws IllegalStateException если список вопросов пуст или равен {@code null}
     */
    private void printFullQuestions(List<Question> questionList) {
        int questionNumber = 0;

        if (questionList == null || questionList.isEmpty()) {
            throw new IllegalStateException(NO_QUESTIONS_FOUND);
        }

        for (var q : questionList) {
            printOneQuestionAndAnswers(q, ++questionNumber);
        }
    }

    /**
     * Выводит один вопрос и все связанные с ним ответы.
     *
     * @param question вопрос для вывода
     * @param questionNumber номер вопроса (начиная с 1)
     */
    private void printOneQuestionAndAnswers(Question question, int questionNumber) {
        int answerNumber = 0;
        this.printQuestion(question.text(), questionNumber);
        for (var a : question.answers()) {
            this.printAnswer(a.text(), answerNumber++);
        }
        ioService.printLine("");
    }

    /**
     * Выводит вариант ответа в заданном формате.
     *
     * @param answerText текст ответа
     * @param answerNumber порядковый номер ответа (начиная с 0)
     */
    private void printAnswer(String answerText, int answerNumber) {
        char letter = (char) (START_CHAR_FOR_ANSWER + answerNumber);
        ioService.printFormattedLine(ANSWER_FORMAT, letter, answerText);
    }

    /**
     * Выводит вопрос в заданном формате.
     *
     * @param questionText текст вопроса
     * @param questionNumber номер вопроса (начиная с 1)
     */
    private void printQuestion(String questionText, int questionNumber) {
        ioService.printFormattedLine(QUESTION_FORMAT, questionNumber, questionText);
    }
}
