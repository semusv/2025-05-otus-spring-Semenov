package ru.otus.hw.controllers.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.otus.hw.controllers.handlers.GlobalExceptionHandler;
import ru.otus.hw.controllers.handlers.GlobalResponseEntityExceptionHandler;
import ru.otus.hw.controllers.handlers.ValidationExceptionHandler;
import ru.otus.hw.dto.GenreDto;
import ru.otus.hw.formatters.ErrorMessageFormatter;
import ru.otus.hw.services.GenreService;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test class for the {@link GenresController}
 */
@WebMvcTest(
        controllers = {
                GenresController.class,
        })
@Import({
        GlobalExceptionHandler.class,
        GlobalResponseEntityExceptionHandler.class,
        ValidationExceptionHandler.class
})
class GenresControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper mapper;

    @MockitoBean
    @Autowired
    private GenreService genreService;

    @MockitoBean
    ErrorMessageFormatter errorMessageFormatter;

    @MockitoBean
    private ValidationExceptionHandler validationExceptionHandler;

    @Test
    @DisplayName("должен вывести полный список жанров")
    void shouldReturnGenreList() throws Exception {
        // given
        List<GenreDto> expectedGenres = List.of(
                new GenreDto(1L, "Роман"),
                new GenreDto(2L, "Фантастика")
        );

        //when
        when(genreService.findAll()).thenReturn(expectedGenres);

        //then
        mockMvc.perform(get("/api/genres")
                        .contentType("application/json"))
                .andExpect(status().isOk())
                .andExpect(content().json(mapper.writeValueAsString(expectedGenres)))
                .andDo(print());
    }


}
