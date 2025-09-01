package ru.otus.hw.controllers.pages;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.otus.hw.config.SecurityConfig;
import ru.otus.hw.controllers.handlers.CustomAccessDeniedHandler;
import ru.otus.hw.controllers.handlers.GlobalExceptionHandler;
import ru.otus.hw.controllers.handlers.GlobalResponseEntityExceptionHandler;
import ru.otus.hw.controllers.handlers.ValidationExceptionHandler;
import ru.otus.hw.formatters.ErrorMessageFormatter;
import ru.otus.hw.services.CustomUserDetailsService;
import ru.otus.hw.services.ErrorHandlingService;
import ru.otus.hw.services.GenreService;

import java.util.stream.Stream;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {
        GenresPagesController.class,
        CustomAccessDeniedHandler.class
})
@Import({
        GlobalExceptionHandler.class,
        GlobalResponseEntityExceptionHandler.class,
        ValidationExceptionHandler.class,
        SecurityConfig.class
})
@DisplayName("Проверка аутентификации для контроллера Жанров")
class GenresPagesControllerSecurityTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private GenreService genreService;

    @MockitoBean
    private CustomUserDetailsService userDetailsService;

    @MockitoBean
    private ErrorMessageFormatter errorMessageFormatter;

    @MockitoBean
    private ErrorHandlingService errorHandlingService;

    @MockitoBean
    private ValidationExceptionHandler validationExceptionHandler;

    static Stream<String> protectedPagesEndpoints() {
        return Stream.of(
                "/genres"
        );
    }

    @ParameterizedTest
    @MethodSource("protectedPagesEndpoints")
    @DisplayName("должен вернуть Redirect, если page endpoint с неавторизованным пользователем")
    void shouldRedirectPagesEndpointUnAuthorized(String endpoint) throws Exception {
        mockMvc.perform(get(endpoint)).andExpect(status().is3xxRedirection());
    }

    @ParameterizedTest
    @MethodSource("protectedPagesEndpoints")
    @DisplayName("должен вернуть OK, если page endpoint с авторизованным пользователем")
    @WithMockUser(username = "user")
    void shouldOkPagesEndpointUnAuthorized(String endpoint) throws Exception {
        mockMvc.perform(get(endpoint)).andExpect(status().isOk());
    }

}