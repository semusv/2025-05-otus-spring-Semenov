package ru.otus.hw.service.converters;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.otus.hw.domain.Answer;
import ru.otus.hw.domain.Question;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;


@SpringBootTest(classes = {QuestionToStringConverterImpl.class})
@DisplayName("Сервис конвертации вопросов в текст")
class QuestionToStringConverterImplTest {

    @Autowired
    private QuestionConverter converter ;

    @Test
    @DisplayName("Должен корректно форматировать вопрос с ответами")
    void ShouldFormatCorrectly() {

        Question question = new Question(
                "What is the capital of France?",
                List.of(
                        new Answer("Paris", true),
                        new Answer("Berlin", false),
                        new Answer("Madrid", false)
                )
        );
        int questionNumber = 1;

        String result = converter.convertQuestionToString(question, questionNumber);

        String expected =
                " 1. What is the capital of France?" + System.lineSeparator() +
                        "    1) Paris" + System.lineSeparator() + // 6 пробелов
                        "    2) Berlin" + System.lineSeparator() +
                        "    3) Madrid";

        assertThat(result).isEqualTo(expected);
    }

    @Test
    @DisplayName("Должен корректно обрабатывать вопрос без ответов")
    void ShouldHandleEmptyAnswers() {
        // Given
        Question question = new Question("Is this a test?", List.of());
        int questionNumber = 42;

        // When
        String result = converter.convertQuestionToString(question, questionNumber);

        // Then
        String expected = "42. Is this a test?";
        assertThat(result).isEqualTo(expected);
    }
}