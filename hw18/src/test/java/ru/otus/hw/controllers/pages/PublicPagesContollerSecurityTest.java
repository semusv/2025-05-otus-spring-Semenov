package ru.otus.hw.controllers.pages;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.otus.hw.config.SecurityConfig;
import ru.otus.hw.controllers.handlers.CustomAccessDeniedHandler;
import ru.otus.hw.controllers.handlers.ValidationExceptionHandler;
import ru.otus.hw.formatters.ErrorMessageFormatter;
import ru.otus.hw.services.CustomUserDetailsService;
import ru.otus.hw.services.ErrorHandlingService;

import java.util.stream.Stream;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SuppressWarnings("unused")
@WebMvcTest(controllers = {
               CustomAccessDeniedHandler.class
})
@Import({
        SecurityConfig.class
})
@DisplayName("Проверка аутентификации для публичных эндпоинтов")
class PublicPagesContollerSecurityTest {

    @Autowired
    private MockMvc mockMvc;


    @MockitoBean
    private CustomUserDetailsService userDetailsService;

    @MockitoBean
    ErrorMessageFormatter errorMessageFormatter;

    @MockitoBean
    ErrorHandlingService errorHandlingService;

    @MockitoBean
    private ValidationExceptionHandler validationExceptionHandler;

    static Stream<String> publicEndpoints() {
        return Stream.of(
                "/login"
        );
    }

    @ParameterizedTest
    @MethodSource("publicEndpoints")
    void whenUnauthenticated_thenPublicEndpointsShouldBeAccessible(String endpoint) throws Exception {
        mockMvc.perform(get(endpoint)).andExpect(status().isOk());
    }
}
