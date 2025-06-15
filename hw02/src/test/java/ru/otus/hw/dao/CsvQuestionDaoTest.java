package ru.otus.hw.dao;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.otus.hw.config.TestFileNameProvider;
import ru.otus.hw.domain.Question;
import ru.otus.hw.exceptions.QuestionReadException;


import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
@DisplayName("Тестирование DAO для чтения вопросов из CSV-файла")
class CsvQuestionDaoTest {
    // Сообщение об ошибке, если в файле нет вопросов.
    private static final String NO_QUESTIONS_FOUND = "No questions found in file";

    // Сообщение об ошибке при чтении файла.
    private static final String ERROR_READING_FILE = "Error reading file '%s'";

    // Сообщение об ошибке при парсинге CSV.
    private static final String ERROR_PARSING_FILE = "Error parsing CSV file";

    // Сообщение об ошибке, если имя файла не указано.
    private static final String FILE_NAME_NOT_PROVIDED = "Test file name is not provided";

    // Сообщение об ошибке, если файл не может быть прочитан.
    private static final String CANT_READ_FILE = "Can't start read file with questions: %s";
    // Файл с пустым содержимым.
    private static final String EMPTY_QUESTIONS_CSV = "empty-questions.csv";
    // Отсутствие имени файла.
    private static final String EMPTY_FILE_NAME = "";
    // Файл, которого нет в ресурсах.
    private static final String NON_EXISTENT_FILE_CSV = "non-existent-file.csv";
    // Файл с тестовыми вопросами.
    private static final String TEST_QUESTIONS_CSV = "test-questions.csv";
    private static final String INVALID_FORMAT_QUESTIONS_CSV = "invalid-format-questions.csv";


    @Mock
    private TestFileNameProvider fileNameProvider;

    @InjectMocks
    private CsvQuestionDao csvQuestionDao;


    @Test
    @DisplayName("Должен корректно читать вопросы из CSV-файла")
    void shouldReadQuestionsFromCsvFile() {

        // Given
        given(fileNameProvider.getTestFileName()).willReturn(TEST_QUESTIONS_CSV);

        // When
        List<Question> questions = csvQuestionDao.findAll();

        // Then
        assertThat(questions).isNotNull().hasSize(3);

        //        What is the capital of France?;Paris%true|London%false|Berlin%false
        Question firstQuestion = questions.get(0);
        assertThat(firstQuestion.text()).isEqualTo("What is the capital of France?");
        assertThat(firstQuestion.answers()).hasSize(3);
        assertThat(firstQuestion.answers().get(0).text()).isEqualTo("Paris");
        assertThat(firstQuestion.answers().get(0).isCorrect()).isTrue();
        assertThat(firstQuestion.answers().get(1).text()).isEqualTo("London");
        assertThat(firstQuestion.answers().get(1).isCorrect()).isFalse();
        assertThat(firstQuestion.answers().get(2).text()).isEqualTo("Berlin");
        assertThat(firstQuestion.answers().get(2).isCorrect()).isFalse();

        //        What is 2+2?;5%false|4%true|3%false
        Question secondQuestion = questions.get(1);
        assertThat(secondQuestion.text()).isEqualTo("What is 2+2?");
        assertThat(secondQuestion.answers()).hasSize(3);
        assertThat(secondQuestion.answers().get(0).text()).isEqualTo("5");
        assertThat(secondQuestion.answers().get(0).isCorrect()).isFalse();
        assertThat(secondQuestion.answers().get(1).text()).isEqualTo("4");
        assertThat(secondQuestion.answers().get(1).isCorrect()).isTrue();
        assertThat(secondQuestion.answers().get(2).text()).isEqualTo("3");
        assertThat(secondQuestion.answers().get(2).isCorrect()).isFalse();

        //        Which Java keyword is used for inheritance?;extends%true|implements%false|inherits%false
        Question thirdQuestion = questions.get(2);
        assertThat(thirdQuestion.text()).isEqualTo("Which Java keyword is used for inheritance?");
        assertThat(thirdQuestion.answers()).hasSize(3);
        assertThat(thirdQuestion.answers().get(0).text()).isEqualTo("extends");
        assertThat(thirdQuestion.answers().get(0).isCorrect()).isTrue();
        assertThat(thirdQuestion.answers().get(1).text()).isEqualTo("implements");
        assertThat(thirdQuestion.answers().get(1).isCorrect()).isFalse();
        assertThat(thirdQuestion.answers().get(2).text()).isEqualTo("inherits");
        assertThat(thirdQuestion.answers().get(2).isCorrect()).isFalse();
    }

    @Test
    @DisplayName("Должен бросать исключение, если файл не найден")
    void shouldThrowExceptionWhenFileNotFound() {
        // Given
        given(fileNameProvider.getTestFileName()).willReturn(NON_EXISTENT_FILE_CSV);

        // When & Then
        assertThatThrownBy(() -> csvQuestionDao.findAll())
                .isInstanceOf(QuestionReadException.class)
                .hasMessageContaining(String.format(ERROR_READING_FILE, NON_EXISTENT_FILE_CSV))
        .cause()
                .isInstanceOf(IOException.class)
                .hasMessageContaining(String.format(CANT_READ_FILE, NON_EXISTENT_FILE_CSV));
    }

    @Test
    @DisplayName("Должен бросать исключение, если файл пустой")
    void shouldThrowExceptionWhenFileIsEmpty() {
        // Given
        given(fileNameProvider.getTestFileName()).willReturn(EMPTY_QUESTIONS_CSV);

        // When & Then
        assertThatThrownBy(() -> csvQuestionDao.findAll())
                .isInstanceOf(QuestionReadException.class)
                .hasMessage(NO_QUESTIONS_FOUND);
    }

    @Test
    @DisplayName("Должен бросать исключение, если имя файла не указано")
    void shouldThrowExceptionWhenFileNameNotProvided() {
        // Given
        given(fileNameProvider.getTestFileName()).willReturn(EMPTY_FILE_NAME);

        // When & Then
        assertThatThrownBy(() -> csvQuestionDao.findAll())
                .isInstanceOf(QuestionReadException.class)
                .hasMessageContaining(String.format(ERROR_READING_FILE, EMPTY_FILE_NAME))
                .cause()
                .isInstanceOf(IOException.class)
                .hasMessageContaining(FILE_NAME_NOT_PROVIDED);
    }

    @Test
    @DisplayName("Должен бросать исключение, если формат записи вопроса некорректный")
    void shouldThrowExceptionWhenQuestionFormatIsIncorrect() {
        // Given
        given(fileNameProvider.getTestFileName()).willReturn(INVALID_FORMAT_QUESTIONS_CSV);

        // When & Then
        assertThatThrownBy(() -> csvQuestionDao.findAll())
                .isInstanceOf(QuestionReadException.class)
                .hasMessageContaining(ERROR_PARSING_FILE);

    }

}