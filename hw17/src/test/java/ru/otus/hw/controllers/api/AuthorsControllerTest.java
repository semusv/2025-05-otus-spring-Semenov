package ru.otus.hw.controllers.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.otus.hw.controllers.handlers.GlobalExceptionHandler;
import ru.otus.hw.controllers.handlers.GlobalResponseEntityExceptionHandler;
import ru.otus.hw.controllers.handlers.ValidationExceptionHandler;
import ru.otus.hw.dto.AuthorDto;
import ru.otus.hw.formatters.ErrorMessageFormatter;
import ru.otus.hw.services.AuthorService;
import ru.otus.hw.services.ErrorHandlingService;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test class for the {@link AuthorsController}
 */
@SuppressWarnings("unused")
@WebMvcTest(
        controllers = {
                AuthorsController.class,
        })
@Import({
        GlobalExceptionHandler.class,
        GlobalResponseEntityExceptionHandler.class,
        ValidationExceptionHandler.class
})
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("Контроллер API Авторов")
class AuthorsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper mapper;

    @MockitoBean
    @Autowired
    private AuthorService authorService;

    @MockitoBean
    @Autowired
    ErrorMessageFormatter errorMessageFormatter;

    @MockitoBean
    private ErrorHandlingService errorHandlingService;


    @Test
    @DisplayName("должен вывести полный список авторов")
    void shouldReturnAuthorList() throws Exception {
        //given
        List<AuthorDto> expectedAuthors = List.of(
                new AuthorDto(1L, "Лев Толстой"),
                new AuthorDto(2L, "Фёдор Достоевский")
        );

        //when
        when(authorService.findAll()).thenReturn(expectedAuthors);

        //then
        mockMvc.perform(get("/api/authors")
                        .contentType("application/json"))
                .andExpect(status().isOk())
                .andExpect(content().json(mapper.writeValueAsString(expectedAuthors)))
                .andDo(print());
    }


}
