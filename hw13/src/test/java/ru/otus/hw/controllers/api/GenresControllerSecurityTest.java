package ru.otus.hw.controllers.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpMethod;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {
        GenresController.class
})
@Import({
        GlobalExceptionHandler.class,
        GlobalResponseEntityExceptionHandler.class,
        ValidationExceptionHandler.class,
        SecurityConfig.class,
        CustomAccessDeniedHandler.class
})
@DisplayName("Проверка аутентификации для API контроллера Жанров")
class GenresControllerSecurityTest {
    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private GenreService genreService;

    @MockitoBean
    private CustomUserDetailsService userDetailsService;

    @MockitoBean
    ErrorMessageFormatter errorMessageFormatter;

    @MockitoBean
    private ValidationExceptionHandler validationExceptionHandler;

    @MockitoBean
    ErrorHandlingService errorHandlingService;


    static Stream<Endpoint> getApiEndpoints() {
        return Stream.of(
                new Endpoint(HttpMethod.GET, "/api/genres", "Get all genres")
        );
    }

    static Stream<Endpoint> deleteApiEndpoints() {
        return Stream.of(
        );
    }

    static Stream<Endpoint> postApiEndpoints() {
        return Stream.of(
        );
    }

    static Stream<Endpoint> putApiEndpoints() {
        return Stream.of(
        );
    }

    @ParameterizedTest
    @MethodSource({"getApiEndpoints", "deleteApiEndpoints", "postApiEndpoints", "putApiEndpoints"})
    @DisplayName("должен вернуть Redirect, если ЛЮБОЙ api endpoint с неавторизованным пользователем")
    void shouldRedirectApiAnyEndpointUnAuthorized(Endpoint endpoint) throws Exception {
        mockMvc.perform(request(endpoint.method, endpoint.path))
                .andExpect(status().is3xxRedirection());
    }

    @ParameterizedTest
    @MethodSource("getApiEndpoints")
    @DisplayName("должен вернуть OK, если GET api endpoint с авторизованным пользователем")
    @WithMockUser(username = "user")
    void shouldOkApiGetEndpointAuthorized(Endpoint endpoint) throws Exception {
        mockMvc.perform(request(endpoint.method, endpoint.path))
                .andExpect(status().isOk());
    }


    record Endpoint(HttpMethod method, String path, String description) {
    }
}