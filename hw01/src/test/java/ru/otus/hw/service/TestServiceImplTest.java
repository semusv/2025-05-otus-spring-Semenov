package ru.otus.hw.service;


import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.otus.hw.dao.QuestionDao;
import ru.otus.hw.domain.Answer;
import ru.otus.hw.domain.Question;


import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.internal.verification.VerificationModeFactory.times;


@ExtendWith(MockitoExtension.class)
class TestServiceImplTest {
    private static final String EXPECTED_QUESTION_FORMAT = "%2d. %s";

    private static final String EXPECTED_ANSWER_FORMAT = "    %c) %s";

    private static final String NO_QUESTIONS_FOUND = "No questions found";

    @Mock
    private IOService ioService;

    @Mock
    private QuestionDao questionDao;

    @InjectMocks
    private TestServiceImpl testService;


    @Test
    @DisplayName("Должен корректно форматировать вопросы и ответы")
    void shouldFormatQuestionsAndAnswersCorrectly() {
        // Arrange
        final String mathQuestion1 = "Сколько будет 2+2?";
        final String mathQuestion2 = "Сколько будет 3+3?";

        final String incorrectAnswer1 = "3";
        final String correctAnswer1 = "4";
        final String incorrectAnswer2 = "1";
        final String correctAnswer2 = "6";
        final String incorrectAnswer3 = "9";

        List<Question> testQuestions = List.of(
                new Question(mathQuestion1, List.of(
                        new Answer(incorrectAnswer1, false),
                        new Answer(correctAnswer1, true)
                )),
                new Question(mathQuestion2, List.of(
                        new Answer(incorrectAnswer2, false),
                        new Answer(correctAnswer2, true),
                        new Answer(incorrectAnswer3, false)
                ))
        );

        given(questionDao.findAll()).willReturn(testQuestions);

        // Act
        testService.executeTest();

        // Assert
        verify(questionDao, times(1)).findAll();

        // Проверка заголовка теста
        verify(ioService, times(1)).printFormattedLine("%nPlease answer the questions below%n");

        // Проверка первого вопроса
        verify(ioService, times(1)).printFormattedLine(EXPECTED_QUESTION_FORMAT, 1, mathQuestion1);
        verify(ioService, times(1)).printFormattedLine(EXPECTED_ANSWER_FORMAT, 'a', incorrectAnswer1);
        verify(ioService, times(1)).printFormattedLine(EXPECTED_ANSWER_FORMAT, 'b', correctAnswer1);

        // Проверка второго вопроса
        verify(ioService, times(1)).printFormattedLine(EXPECTED_QUESTION_FORMAT, 2, mathQuestion2);
        verify(ioService, times(1)).printFormattedLine(EXPECTED_ANSWER_FORMAT, 'a', incorrectAnswer2);
        verify(ioService, times(1)).printFormattedLine(EXPECTED_ANSWER_FORMAT, 'b', correctAnswer2);
        verify(ioService, times(1)).printFormattedLine(EXPECTED_ANSWER_FORMAT, 'c', incorrectAnswer3);

        // Проверка пустых строк между вопросами
        verify(ioService, times(2)).printLine("");
    }

    @Test
    @DisplayName("Должен выдавать ошибку при null вопросах")
    void shouldThrowExceptionWhenNullQuestions() {
        given(questionDao.findAll()).willReturn(null);

        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> testService.executeTest());
        assertTrue(exception.getMessage().contains(NO_QUESTIONS_FOUND));
    }

    @Test
    @DisplayName("Должен выдавать ошибку при пустом списке вопросов")
    void shouldThrowExceptionWhenEmptyListQuestions() {
        given(questionDao.findAll()).willReturn(List.of());

        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> testService.executeTest());
        assertTrue(exception.getMessage().contains(NO_QUESTIONS_FOUND));
    }

}