package ru.otus.hw.dao;

import com.opencsv.bean.CsvToBeanBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.otus.hw.config.TestFileNameProvider;
import ru.otus.hw.dao.dto.QuestionDto;
import ru.otus.hw.domain.Question;
import ru.otus.hw.exceptions.QuestionReadException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;


@Component
@RequiredArgsConstructor
public class CsvQuestionDao implements QuestionDao {
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

    // Провайдер имени файла с вопросами.
    private final TestFileNameProvider fileNameProvider;


    @Override
    public List<Question> findAll() {
        List<QuestionDto> questionDtos;

        try (InputStream inputStream = getResourceInputStream();
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            questionDtos = parseCSVDataFromReader(reader);
        } catch (IOException e) {
            throw new QuestionReadException(
                    String.format(ERROR_READING_FILE,
                            fileNameProvider.getTestFileName()), e);
        }
        if (questionDtos.isEmpty()) {
            throw new QuestionReadException(NO_QUESTIONS_FOUND);
        }

        return questionDtos.stream().map(QuestionDto::toDomainObject).toList();
    }

    private static List<QuestionDto> parseCSVDataFromReader(BufferedReader reader) {
        var csvToBean = new CsvToBeanBuilder<QuestionDto>(reader)
                .withSkipLines(1)
                .withSeparator(';')
                .withType(QuestionDto.class)
                .withOrderedResults(true)
                .build();
        try {
            return csvToBean.parse();
        } catch (RuntimeException e) {
            throw new QuestionReadException(ERROR_PARSING_FILE, e);
        }
    }


    private InputStream getResourceInputStream() throws IOException {

        String fileName = fileNameProvider.getTestFileName();
        if (fileName == null || fileName.isEmpty()) {
            throw new IOException(FILE_NAME_NOT_PROVIDED);
        }

        InputStream inputStream = getClass().getClassLoader()
                .getResourceAsStream(fileName);
        if (inputStream == null) {
            throw new IOException(String.format(CANT_READ_FILE, fileName));
        } else {
            return inputStream;
        }
    }
}
