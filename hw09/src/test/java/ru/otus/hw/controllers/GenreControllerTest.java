package ru.otus.hw.controllers;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.otus.hw.dto.GenreDto;
import ru.otus.hw.services.GenreService;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(GenreController.class)
class GenreControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    @Autowired
    private GenreService genreService;

    @Test
    @DisplayName("Получение списка жанров должно возвращать страницу с жанрами")
    void getAll_ShouldReturnGenresListPage() throws Exception {
        // given
        List<GenreDto> expectedGenres = List.of(
                new GenreDto(1L, "Роман"),
                new GenreDto(2L, "Фантастика")
        );
        when(genreService.findAll()).thenReturn(expectedGenres);

        // when & then
        mockMvc.perform(get("/genres"))
                .andExpect(status().isOk())
                .andExpect(view().name("genres-list"))
                .andExpect(model().attributeExists("genres"))
                .andExpect(model().attribute("genres", expectedGenres));
    }
}