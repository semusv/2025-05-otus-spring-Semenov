package ru.otus.hw.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.otus.hw.dao.QuestionDao;
import ru.otus.hw.domain.Question;
import ru.otus.hw.domain.Student;
import ru.otus.hw.domain.TestResult;
import ru.otus.hw.exceptions.QuestionReadException;
import ru.otus.hw.service.converters.QuestionConverter;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TestServiceImpl implements TestService {
    // Заголовок с вопросами
    private static final String TOPIC_LINE = "%nPlease answer the questions below%n";

    // Начальный и конечный диапазон ответа
    private static final int MIN_ANSWER_NUMBER = 1;

    // Текст приглашения пользователя к ответу на вопрос
    private static final String ANSWER_PROMPT_FORMAT = "Enter the answer for the question #%d";

    // Текст при неверном ответе на вопрос
    private static final String INVALID_ANSWER_MESSAGE = "Please enter a valid answer number";

    // Сообщение об ошибке чтения вопросов
    private static final String QUESTION_READ_ERROR_FORMAT = "Ошибка чтения вопросов: %s";

    // Сообщение когда слишком много неправильных ответов
    private static final String TOO_MANY_ANSWERS_ERROR_MESSAGE = "Слишком много неправильных ответов";

    private final IOService ioService;

    private final QuestionDao questionDao;

    private final QuestionConverter questionConverter;

    @Override
    public TestResult executeTestFor(Student student) {
        ioService.printFormattedLine(TOPIC_LINE);
        TestResult testResult = new TestResult(student);
        try {
            List<Question> questions = questionDao.findAll();
            processQuestions(questions, testResult);
        } catch (QuestionReadException e) {
            ioService.printFormattedLine(QUESTION_READ_ERROR_FORMAT, e.getMessage());
            testResult.clearResults();
        } catch (IllegalArgumentException e) {
            ioService.printLine(TOO_MANY_ANSWERS_ERROR_MESSAGE);
            testResult.clearResults();
        }

        return testResult;
    }

    private void processQuestions(List<Question> questionList, TestResult testResult) {
        int questionNumber = 1;
        for (var question : questionList) {
            ioService.printLine(questionConverter.convertQuestionToString(question, questionNumber));
            var isAnswerValid = processUserAnswer(question, questionNumber);
            testResult.applyAnswer(question, isAnswerValid);
            questionNumber++;
        }
    }

    private boolean processUserAnswer(Question question, int questionNumber) {
        int maxAnswerNumber = question.answers().size();
        String promptText = String.format(ANSWER_PROMPT_FORMAT, questionNumber); // Получить ответ

        int userAnswer = ioService.readIntForRangeWithPrompt(
                MIN_ANSWER_NUMBER,
                maxAnswerNumber,
                promptText,
                INVALID_ANSWER_MESSAGE
        );
        ioService.printLine("");
        return isAnswerCorrect(question, userAnswer); // Проверить правильность ответа
    }


    private boolean isAnswerCorrect(Question question, int userAnswer) {
        return question.answers().get(userAnswer - MIN_ANSWER_NUMBER).isCorrect();
    }
}
