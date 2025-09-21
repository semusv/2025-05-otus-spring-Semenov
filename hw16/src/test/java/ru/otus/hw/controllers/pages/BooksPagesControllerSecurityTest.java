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
import ru.otus.hw.services.BookService;
import ru.otus.hw.services.CustomUserDetailsService;
import ru.otus.hw.services.ErrorHandlingService;

import java.util.stream.Stream;


import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {
        BooksPagesController.class,
        CustomAccessDeniedHandler.class
})
@Import({
        GlobalExceptionHandler.class,
        GlobalResponseEntityExceptionHandler.class,
        ValidationExceptionHandler.class,
        SecurityConfig.class
})
@DisplayName("Проверка аутентификации для контроллера Книг")
class BooksPagesControllerSecurityTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BookService bookService;

    @MockitoBean
    private CustomUserDetailsService userDetailsService;

    @MockitoBean
    ErrorMessageFormatter errorMessageFormatter;

    @MockitoBean
    ErrorHandlingService errorHandlingService;

    @MockitoBean
    private ValidationExceptionHandler validationExceptionHandler;

    static Stream<String> protectedPagesEndpoints() {
        return Stream.of(
                "/",
                "/books",
                "/books/1",
                "/books/1/edit",
                "/books/new"
        );
    }

    static Stream<String> lockedUrlsWithRoles() {
        return Stream.of(
                "/books/1/edit",
                "/books/new"
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

    @ParameterizedTest
    @MethodSource("lockedUrlsWithRoles")
    @DisplayName("должен вернуть OK, если пользователь с ролью User ")
    @WithMockUser(username = "user" , roles = {"USER"})
    void shouldOkUrlEndpointUserAuthorized(String endpoint) throws Exception {
        mockMvc.perform(get(endpoint)).andExpect(status().isOk());
    }

    @ParameterizedTest
    @MethodSource("lockedUrlsWithRoles")
    @DisplayName("должен вернуть OK, если пользователь с ролью Admin ")
    @WithMockUser(username = "admin" , roles = {"ADMIN"})
    void shouldOkUrlEndpointAdminAuthorized(String endpoint) throws Exception {
        mockMvc.perform(get(endpoint)).andExpect(status().isOk());
    }

    @ParameterizedTest
    @MethodSource("lockedUrlsWithRoles")
    @DisplayName("должен вернуть Redirect, если пользователь без нужных ролей ")
    @WithMockUser(username = "guest", roles = {"GUEST"} )
    void shouldOkUrlEndpointSomeUserAuthorized(String endpoint) throws Exception {
        mockMvc.perform(get(endpoint)).andExpect(status().is3xxRedirection());
    }


}