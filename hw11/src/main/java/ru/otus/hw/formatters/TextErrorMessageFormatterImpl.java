package ru.otus.hw.formatters;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StreamUtils;
import ru.otus.hw.config.ErrorTemplateConfig;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class TextErrorMessageFormatterImpl implements ErrorMessageFormatter {

    private String templateContent;

    private final ErrorTemplateConfig errorTemplateConfig;

    @Override
    public String format(Exception ex, ServerHttpRequest request, String errorText) {


        String message = errorText;
        if (errorText == null || errorText.isEmpty()) {
            message = ex.getMessage() != null ? ex.getMessage() : "<no message>";
        }

        return templateContent.formatted(
                ex.getClass().getSimpleName(),
                message,
                request.getPath(),
                request.getMethod(),
                formatParameters(request.getQueryParams()),
                formatHeaders(request.getHeaders()),
                formatStackTrace(ex)
        );

    }

    @PostConstruct
    public void init() throws IOException {
        try (InputStream inputStream = getResourceInputStream()) {
            templateContent = StreamUtils.copyToString(
                    getResourceInputStream(),
                    StandardCharsets.UTF_8);
        }
    }

    private InputStream getResourceInputStream() throws IOException {
        String fileName = errorTemplateConfig.getError();
        if (fileName == null || fileName.isEmpty()) {
            throw new IOException("Test file name is not provided");
        }
        InputStream inputStream = getClass().getClassLoader()
                .getResourceAsStream(fileName);
        if (inputStream == null) {
            throw new IOException(String.format("Can't start read file with error template: %s", fileName));
        } else {
            return inputStream;
        }
    }


    private String formatParameters(MultiValueMap<String, String> parameterMap) {
        if (parameterMap.isEmpty()) {
            return "<none>";
        }
        return parameterMap.entrySet().stream()
                .map(entry -> String.format("%s=%s",
                        entry.getKey(),
                        entry.getValue()))
                .collect(Collectors.joining(", "));
    }

    private String formatHeaders(HttpHeaders headers) {
        return headers.entrySet().stream()
                .map(entry -> String.format("║    • %s: %s",
                        entry.getKey(),
                        String.join(", ", entry.getValue())))
                .collect(Collectors.joining("\n"));
    }

    private String formatStackTrace(Exception ex) {
        return Arrays.stream(ExceptionUtils.getStackTrace(ex).split("\n"))
                .map(line -> "║  " + line)
                .collect(Collectors.joining("\n"));
    }

}
