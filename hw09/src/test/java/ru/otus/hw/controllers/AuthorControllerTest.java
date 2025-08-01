package ru.otus.hw.controllers;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.otus.hw.dto.AuthorDto;
import ru.otus.hw.services.AuthorService;

import java.util.List;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

/**
 * Test class for the {@link AuthorController}
 */
@WebMvcTest({AuthorController.class})
class AuthorControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    @Autowired
    private AuthorService authorService;

    @Test
    @DisplayName("должен вывести полный список авторов")
    void getAll() throws Exception {
        //given
        List<AuthorDto> expectedAuthors = List.of(
                new AuthorDto(1L, "Лев Толстой"),
                new AuthorDto(2L, "Фёдор Достоевский")
        );
        when(authorService.getAll()).thenReturn(expectedAuthors);

        //when & then
        mockMvc.perform(get("/authors"))
                .andExpect(status().isOk())
                .andExpect(view().name("authors-list"))
                .andExpect(model().attributeExists("authors"))
                .andExpect(model().attribute("authors", expectedAuthors));

        verify(authorService, times(1)).getAll();
    }


}
