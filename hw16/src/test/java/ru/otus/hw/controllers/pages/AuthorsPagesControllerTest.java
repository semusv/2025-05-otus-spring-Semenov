package ru.otus.hw.controllers.pages;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.RestControllerAdvice;


import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

/**
 * Test class for the {@link AuthorsPagesController}
 */
@WebMvcTest(
        controllers = AuthorsPagesController.class,
        excludeFilters = {
                @ComponentScan.Filter(
                        type = FilterType.ANNOTATION,
                        classes = ControllerAdvice.class),
                @ComponentScan.Filter(
                        type = FilterType.ANNOTATION,
                        classes = RestControllerAdvice.class),
        }
)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("Контроллер страниц авторов")
class AuthorsPagesControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("должен вывести полный список авторов")
    void getAll() throws Exception {
        //given

        //when & then
        mockMvc.perform(get("/authors"))
                .andExpect(status().isOk())
                .andExpect(view().name("authors-list"));

    }


}
