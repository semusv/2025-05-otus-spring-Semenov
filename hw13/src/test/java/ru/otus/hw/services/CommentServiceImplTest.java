package ru.otus.hw.services;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import ru.otus.hw.dto.CommentDto;
import ru.otus.hw.exceptions.EntityNotFoundException;
import ru.otus.hw.mappers.CommentMapperImpl;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

@DisplayName("Сервис для работы с комментариями")
@DataJpaTest
@Import({CommentServiceImpl.class,
        CommentMapperImpl.class})
class CommentServiceImplTest {

    @Autowired
    private CommentService commentService;

    @Autowired
    @MockitoBean
    private AclServiceWrapperService aclServiceWrapperService;

    @Test
    @DisplayName("должен загружать комментарий по id")
    void findById_shouldReturnComment() {
        CommentDto result = commentService.findById(1L);

        assertThat(result).isNotNull();
        assertThat(result.text()).isNotBlank();
        assertThat(result.bookId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("должен выбрасывать исключение при поиске несуществующего комментария")
    void shouldThrowForNonExistingId() {
        assertThatThrownBy(() -> commentService.findById(999L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Comment with id 999 not found");
    }

    @Test
    @DisplayName("должен загружать все комментарии для книги")
    void shouldReturnAllCommentsForBook() {
        List<CommentDto> result = commentService.findByBookId(1L);

        assertThat(result).hasSize(3)
                .allMatch(commentDto -> !commentDto.text().isBlank())
                .allMatch(commentDto -> commentDto.bookId() == 1L);
    }

    @Test
    @DisplayName("должен выбрасывать исключение при поиске комментариев для несуществующей книги")
    void shouldThrowForNonExistingBookId() {
        assertThatThrownBy(() -> commentService.findByBookId(999L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Book with id 999 not found");
    }

    @Test
    @DisplayName("должен удалять комментарий")
    void shouldDeleteComment() {
        long commentId = 1L;
        assertThat(commentService.findById(commentId)).isNotNull();

        commentService.deleteById(commentId);

        assertThatThrownBy(() -> commentService.findById(commentId))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Comment with id 1 not found");
    }

    @Test
    @DisplayName("должен создавать новый комментарий")
    void shouldCreateNewComment() {
        String commentText = "New Test Comment";
        long bookId = 1L;
        CommentDto commentDto = new CommentDto(0L, commentText, bookId);

        CommentDto result = commentService.insert(commentDto);

        assertThat(result.id()).isPositive();
        assertThat(result.text()).isEqualTo(commentText);
        assertThat(result.bookId()).isEqualTo(bookId);

        CommentDto savedComment = commentService.findById(result.id());
        assertThat(savedComment).isNotNull();
        assertThat(savedComment.text()).isEqualTo(commentText);
        assertThat(savedComment.bookId()).isEqualTo(bookId);
    }

    @Test
    @DisplayName("должен обновлять существующий комментарий")
    void shouldUpdateExistingComment() {
        long commentId = 1L;
        String updatedText = "Updated Text";
        long bookId = 1L;
        CommentDto commentDto = new CommentDto(commentId, updatedText, bookId);

        CommentDto result = commentService.update(commentDto);

        assertThat(result.id()).isEqualTo(commentId);
        assertThat(result.text()).isEqualTo(updatedText);

        CommentDto updatedComment = commentService.findById(commentId);
        assertThat(updatedComment).isNotNull();
        assertThat(updatedComment.text()).isEqualTo(updatedText);
    }

    @Test
    @DisplayName("должен выбрасывать исключение при обновлении несуществующего комментария")
    void shouldThrowForNonExistingComment() {
        CommentDto commentDto = new CommentDto(999L, "Some text", 1L);

        assertThatThrownBy(() -> commentService.update(commentDto))
                .isInstanceOf(EntityNotFoundException.class);
    }
}