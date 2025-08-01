package ru.otus.hw.controllers;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.otus.hw.dto.CommentDto;
import ru.otus.hw.services.CommentService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CommentController.class)
class CommentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    @Autowired
    private CommentService commentService;

    @Test
    @DisplayName("Добавление комментария должно перенаправлять на страницу книги")
    void addCommentToBook_ShouldRedirectToBookPage() throws Exception {
        // given
        long bookId = 1L;
        CommentDto commentDto = new CommentDto(0L, "Новый комментарий", bookId);

        // when & then
        mockMvc.perform(post("/books/{id}/comments/add", bookId)
                        .flashAttr("comment", commentDto))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/books/" + bookId + "/display#comments-section"));

        verify(commentService, times(1)).insert(any(CommentDto.class));
    }

    @Test
    @DisplayName("Удаление комментария должно перенаправлять на страницу редактирования с сообщением")
    void deleteComment_ShouldRedirectToEditPageWithMessage() throws Exception {
        // given
        long bookId = 1L;
        long commentId = 10L;

        // when & then
        mockMvc.perform(post("/books/{id}/comments/{commentId}/delete", bookId, commentId))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/books/" + bookId + "/edit#comments-section"))
                .andExpect(flash().attributeExists("message"))
                .andExpect(flash().attribute("message", "Комментарий успешно удален"));

        verify(commentService, times(1)).deleteById(commentId);
    }
}