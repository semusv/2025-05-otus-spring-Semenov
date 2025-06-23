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

    // Начальный и конечный диапазон ответа
    private static final int MIN_ANSWER_NUMBER = 1;

    private final LocalizedIOService ioService;

    private final QuestionDao questionDao;

    private final QuestionConverter questionConverter;

    @Override
    public TestResult executeTestFor(Student student) {
        processTopic();

        TestResult testResult = new TestResult(student);
        try {
            List<Question> questions = questionDao.findAll();
            processQuestions(questions, testResult);
        } catch (QuestionReadException e) {
            ioService.printFormattedLineLocalized("TestService.questions.read.error",e.getMessage());
            testResult.clearResults();
        } catch (IllegalArgumentException e) {
            ioService.printLineLocalized("TestService.too.many.answers.error.message");
            testResult.clearResults();
        }
        return testResult;
    }

    private void processTopic() {
        ioService.printLine("");
        ioService.printLineLocalized("TestService.answer.the.questions");
        ioService.printLine("");
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

        ioService.printFormattedLineLocalized("TestService.question.answer.prompt", questionNumber);
        int userAnswer = ioService.readIntForRangeLocalized(
                MIN_ANSWER_NUMBER,
                maxAnswerNumber,
                "TestService.invalid.answer.message"
        );
        ioService.printLine("");

        return isAnswerCorrect(question, userAnswer); // Проверить правильность ответа
    }

    private boolean isAnswerCorrect(Question question, int userAnswer) {
        return question.answers().get(userAnswer - MIN_ANSWER_NUMBER).isCorrect();
    }

}
