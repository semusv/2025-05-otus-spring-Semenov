package ru.otus.hw.dao;

import com.opencsv.bean.CsvToBeanBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import ru.otus.hw.config.TestFileNameProvider;
import ru.otus.hw.dao.dto.QuestionDto;
import ru.otus.hw.domain.Question;
import ru.otus.hw.exceptions.QuestionReadException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import java.util.List;

@RequiredArgsConstructor
public class CsvQuestionDao implements QuestionDao {
    public static final int SKIP_LINES = 1;

    public static final char SEPARATOR = ';';

    private final TestFileNameProvider fileNameProvider;

    @Override
    public List<Question> findAll() {
        // Использовать CsvToBean
        // https://opencsv.sourceforge.net/#collection_based_bean_fields_one_to_many_mappings
        // Использовать QuestionReadException
        // Про ресурсы: https://mkyong.com/java/java-read-a-file-from-resources-folder/
        try (InputStream inputStream = getResourceInputStream();
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            var csvToBean = new CsvToBeanBuilder<QuestionDto>(reader)
                    .withSkipLines(SKIP_LINES)
                    .withSeparator(SEPARATOR)
                    .withType(QuestionDto.class)
                    .withOrderedResults(true)
                    .build();
            List<QuestionDto> questionDtos = csvToBean.stream().toList();
            return questionDtos.stream().map(QuestionDto::toDomainObject).toList();
        } catch (IOException e) {
            throw new QuestionReadException("Error working with CSV file", e);
        } catch (RuntimeException e) {
            throw new QuestionReadException("Error parsing CSV file", e);
        }
    }

    private InputStream getResourceInputStream() throws IOException {
        String fileName = fileNameProvider.getTestFileName();
        ClassPathResource resource = new ClassPathResource(fileName);
        InputStream inputStream;
        try {
            inputStream = resource.getInputStream();
        } catch (IOException e) {
            throw new IOException("Can't start read file with questions: " + fileName, e);
        }
        return inputStream;
    }


}
