package ru.otus.hw.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import ru.otus.hw.dao.QuestionDao;
import ru.otus.hw.domain.Answer;
import ru.otus.hw.domain.Question;
import ru.otus.hw.domain.Student;
import ru.otus.hw.domain.TestResult;
import ru.otus.hw.exceptions.QuestionReadException;
import ru.otus.hw.service.converters.QuestionConverter;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@SpringBootTest(classes = {TestServiceImpl.class})
@DisplayName("Сервис выполнения тестирования студента")
class TestServiceImplTest {


    @MockitoBean
    private LocalizedIOService ioService;

    @MockitoBean
    private QuestionDao questionDao;

    @MockitoBean
    private QuestionConverter questionConverter;

    @Autowired
    private TestServiceImpl testService;

    private Student student;

    private List<Question> testQuestions;

    @BeforeEach
    void setUp() {
        student = new Student("Ivan", "Ivanov");

        Answer correctAnswer = new Answer("Correct answer", true);
        Answer wrongAnswer = new Answer("Wrong answer", false);

        testQuestions = List.of(
                new Question("Question 1", List.of(wrongAnswer, correctAnswer, wrongAnswer, wrongAnswer)),
                new Question("Question 2", List.of(wrongAnswer, wrongAnswer, correctAnswer))
        );
    }

    @Test
    @DisplayName("Должен вернуть результат выполнения теста с корректным количеством правильных ответов")
    void ShouldReturnTestResultWithCorrectAnswers() {
        String formattedSomeTestQuestion = "Formatted some test question";

        given(questionDao.findAll()).willReturn(testQuestions);
        given(questionConverter.convertQuestionToString(any(Question.class), anyInt())).willReturn(formattedSomeTestQuestion);
        given(ioService.readIntForRangeLocalized(anyInt(), anyInt(), anyString()))
                .willReturn(2)
                .willReturn(3);

        //Action
        TestResult result = testService.executeTestFor(student);

        //Assertions
        assertThat(result).isNotNull();
        assertThat(result.getStudent()).isEqualTo(student);
        assertThat(result.getRightAnswersCount()).isEqualTo(2);

        //verify
        verify(questionDao, times(1)).findAll();
        verify(ioService, times(1)).printLineLocalized("TestService.answer.the.questions");
        verify(questionConverter, times(2)).convertQuestionToString(any(Question.class), anyInt());
        verify(ioService, times(2)).printFormattedLineLocalized(eq("TestService.question.answer.prompt"), anyInt());
        verify(ioService, times(2)).readIntForRangeLocalized(anyInt(), anyInt(), eq("TestService.invalid.answer.message"));
        verify(ioService, times(2)).printLine(formattedSomeTestQuestion);


    }

    @Test
    @DisplayName("Должен выкинуть сообщение об ошибке и вернуть результат выполнения теста с нулевым количеством правильных ответов, когда неправильно выбирает номер ответа много раз подряд")
    void ShouldThrowExceptionWhenSelectWrongAnswersTooMuch() {
        String formattedSomeTestQuestion = "Formatted some test question";
        String errorMessage = "Error during reading int value";

        given(questionDao.findAll()).willReturn(testQuestions);
        given(questionConverter.convertQuestionToString(any(Question.class), anyInt())).willReturn(formattedSomeTestQuestion);
        given(ioService.readIntForRangeLocalized(anyInt(), anyInt(), anyString()))
                .willReturn(1)
                .willThrow(new IllegalArgumentException(errorMessage));


        //Action
        TestResult result = testService.executeTestFor(student);

        //Assertions
        assertThat(result).isNotNull();
        assertThat(result.getStudent()).isEqualTo(student);
        assertThat(result.getRightAnswersCount()).isEqualTo(0);
        assertThat(result.getAnsweredQuestions().size()).isEqualTo(0);

        //verify
        verify(questionDao, times(1)).findAll();
        verify(ioService, times(1)).printLineLocalized("TestService.answer.the.questions");
        verify(ioService, times(1)).printLineLocalized("TestService.too.many.answers.error.message");
        verify(ioService, times(2)).printFormattedLineLocalized(eq("TestService.question.answer.prompt"), anyInt());
        verify(ioService, times(2)).readIntForRangeLocalized(anyInt(), anyInt(), eq("TestService.invalid.answer.message"));

    }

    @Test
    @DisplayName("Должен вернуть результат выполнения теста с нулевым количеством правильных ответов")
    void ShouldReturnTestResultWithZeroAnswers() {
        String formattedSomeTestQuestion = "Formatted some test question";

        given(questionDao.findAll()).willReturn(testQuestions);
        given(questionConverter.convertQuestionToString(any(Question.class), anyInt())).willReturn(formattedSomeTestQuestion);
        given(ioService.readIntForRangeLocalized(anyInt(), anyInt(), anyString()))
                .willReturn(1)
                .willReturn(2);

        //Action
        TestResult result = testService.executeTestFor(student);

        //Assertions
        assertThat(result).isNotNull();
        assertThat(result.getStudent()).isEqualTo(student);
        assertThat(result.getRightAnswersCount()).isEqualTo(0);

        //verify
        verify(questionDao, times(1)).findAll();
        verify(questionConverter, times(2)).convertQuestionToString(any(Question.class), anyInt());
        verify(ioService, times(2)).printFormattedLineLocalized(eq("TestService.question.answer.prompt"), anyInt());
        verify(ioService, times(2)).readIntForRangeLocalized(anyInt(), anyInt(), eq("TestService.invalid.answer.message"));
        verify(ioService, times(2)).printLine(formattedSomeTestQuestion);
        verify(ioService, times(4)).printLine("" );
    }

    @Test
    @DisplayName("Должен вывести сообщение об ошибке получении списка вопросов")
    void ShouldReturnErrorMessageWhenQuestionsAreAbsent() {
        String errorMessage = "Error";
        given(questionDao.findAll()).willThrow(new QuestionReadException(errorMessage));

        //Captor для захвата аргументов
        ArgumentCaptor<String> messageCaptor = ArgumentCaptor.forClass(String.class);


        //Action
        TestResult result = testService.executeTestFor(student);

        //Assertions
        assertThat(result).isNotNull();
        assertThat(result.getStudent()).isEqualTo(student);
        assertThat(result.getRightAnswersCount()).isEqualTo(0);

        //verify
        verify(questionDao, times(1)).findAll();
        verify(ioService, times(1)).printFormattedLineLocalized(anyString(), messageCaptor.capture());
        assertThat(messageCaptor.getValue()).isEqualTo(errorMessage);

    }

}