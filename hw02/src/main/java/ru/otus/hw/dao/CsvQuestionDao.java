package ru.otus.hw.dao;

import com.opencsv.bean.CsvToBeanBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import ru.otus.hw.config.TestFileNameProvider;
import ru.otus.hw.dao.dto.QuestionDto;
import ru.otus.hw.domain.Question;
import ru.otus.hw.exceptions.QuestionReadException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class CsvQuestionDao implements QuestionDao {
    // Количество строк, которые нужно пропустить при чтении CSV (например, заголовок).
    public static final int SKIP_LINES = 1;

    // Разделитель полей в CSV-файле.
    public static final char SEPARATOR = ';';

    // Сообщение об ошибке, если в файле нет вопросов.
    private static final String NO_QUESTIONS_FOUND = "No questions found in file";

    // Сообщение об ошибке при чтении файла.
    private static final String ERROR_READING_FILE = "Error reading file '%s'";

    // Сообщение об ошибке при парсинге CSV.
    private static final String ERROR_PARSING_FILE = "Error parsing CSV file '%s'";

    // Сообщение об ошибке, если имя файла не указано.
    private static final String FILE_NAME_NOT_PROVIDED = "Test file name is not provided";

    // Сообщение об ошибке, если файл не может быть прочитан.
    private static final String CANT_READ_FILE = "Can't start read file with questions: %s";

    // Провайдер имени файла с вопросами.
    private final TestFileNameProvider fileNameProvider;

    /**
     * Возвращает список всех вопросов из CSV-файла.
     *
     * @return список вопросов ({@link Question})
     * @throws QuestionReadException если:
     *                               - файл не найден или не может быть прочитан ({@link IOException}),
     *                               - произошла ошибка парсинга CSV ({@link RuntimeException}),
     *                               - в файле нет вопросов.
     */
    @Override
    public List<Question> findAll() {

        try (InputStream inputStream = getResourceInputStream();
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            var csvToBean = new CsvToBeanBuilder<QuestionDto>(reader)
                    .withSkipLines(SKIP_LINES)
                    .withSeparator(SEPARATOR)
                    .withType(QuestionDto.class)
                    .withOrderedResults(true)
                    .build();
            List<QuestionDto> questionDtos = csvToBean.stream().toList();
            if (questionDtos.isEmpty()) {
                throw new QuestionReadException(NO_QUESTIONS_FOUND);
            }
            return questionDtos.stream().map(QuestionDto::toDomainObject).toList();
        } catch (IOException e) {
            throw new QuestionReadException(
                    String.format(ERROR_READING_FILE,
                            fileNameProvider.getTestFileName()), e);
        } catch (RuntimeException e) {
            throw new QuestionReadException(
                    String.format(ERROR_PARSING_FILE,
                            fileNameProvider.getTestFileName()), e);
        }
    }

    /**
     * Возвращает {@link InputStream} для чтения CSV-файла с вопросами.
     *
     * @return поток для чтения файла
     * @throws IOException если:
     *                     - имя файла не указано ({@value #FILE_NAME_NOT_PROVIDED}),
     *                     - файл не найден ({@value #CANT_READ_FILE}).
     */
    private InputStream getResourceInputStream() throws IOException {

        String fileName = fileNameProvider.getTestFileName();
        if (fileName == null || fileName.isEmpty()) {
            throw new IOException(FILE_NAME_NOT_PROVIDED);
        }

        InputStream inputStream = getClass().getClassLoader()
                .getResourceAsStream(fileName);
        if (inputStream == null) {
            throw new IOException(CANT_READ_FILE + fileName);
        } else {
            return inputStream;
        }
    }
}
