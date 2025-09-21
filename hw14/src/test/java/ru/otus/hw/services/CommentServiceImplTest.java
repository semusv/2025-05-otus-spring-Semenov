package ru.otus.hw.services;

import jakarta.validation.ValidationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import ru.otus.hw.mappers.AuthorMapper;
import ru.otus.hw.mappers.CommentMapper;
import ru.otus.hw.dto.CommentDto;
import ru.otus.hw.exceptions.EntityNotFoundException;
import ru.otus.hw.services.validators.CommentValidatorImpl;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

@DisplayName("Интеграционный тест для сервиса комментариев")
@DataJpaTest
@Import({CommentServiceImpl.class,
        CommentMapper.class,
        AuthorMapper.class,
        CommentValidatorImpl.class})
class CommentServiceImplTest {

    private final CommentService commentService;

    @Autowired
    CommentServiceImplTest(CommentService commentService) {
        this.commentService = commentService;
    }

    @Test
    @DisplayName("Должен находить комментарий по id без LazyInitializationException")
    void findById_shouldNotThrowLazyException() {
        Optional<CommentDto> result = commentService.findById(1L);

        assertThat(result).isPresent();
        assertThat(result.get().text()).isNotBlank();
        assertThat(result.get().bookId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("Должен возвращать пустой Optional при поиске несуществующего комментария")
    void findById_shouldReturnEmptyForNonExistingId() {
        Optional<CommentDto> result = commentService.findById(999L);
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Должен находить комментарии по bookId без LazyInitializationException")
    void findByBookId_shouldNotThrowLazyException() {
        List<CommentDto> result = commentService.findByBookId(1L);

        assertThat(result).hasSize(3)
                .allMatch(commentDto -> !commentDto.text().isBlank())
                .allMatch(commentDto -> commentDto.bookId() == 1L);
    }

    @Test
    @DisplayName("Должен возвращать пустой список для несуществующего bookId")
    void findByBookId_shouldReturnEmptyListForNonExistingBookId() {
        assertThatThrownBy(() -> commentService.findByBookId(999L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Book with id 999 not found");
    }

    @Test
    @DisplayName("Должен удалять комментарий по id")
    void deleteById_shouldDeleteComment() {
        long commentId = 1L;
        assertThat(commentService.findById(commentId)).isPresent();

        commentService.deleteById(commentId);

        assertThat(commentService.findById(commentId)).isEmpty();
    }

    @Test
    @DisplayName("Должен бросать исключение при удалении несуществующего комментария")
    void deleteById_shouldThrowForNonExistingId() {
        assertThatThrownBy(() -> commentService.deleteById(999L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Comment with id 999 not found");
    }

    @Test
    @DisplayName("Должен создавать новый комментарий")
    void insert_shouldCreateNewComment() {
        String commentText = "New Test Comment";
        long bookId = 1L;

        CommentDto result = commentService.insert(commentText, bookId);

        assertThat(result.id()).isPositive();
        assertThat(result.text()).isEqualTo(commentText);
        assertThat(result.bookId()).isEqualTo(bookId);

        Optional<CommentDto> savedComment = commentService.findById(result.id());
        assertThat(savedComment).isPresent();
        assertThat(savedComment.get().text()).isEqualTo(commentText);
        assertThat(savedComment.get().bookId()).isEqualTo(bookId);
    }

    @Test
    @DisplayName("Должен бросать исключение при создании комментария для несуществующей книги")
    void insert_shouldThrowForNonExistingBook() {
        assertThatThrownBy(() -> commentService.insert("Test", 999L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Book with id 999 not found");
    }


    @Test
    @DisplayName("Должен бросать исключение при создании комментария с пустым текстом")
    void insert_shouldThrowForEmptyText() {
        assertThatThrownBy(() -> commentService.insert("", 1L))
                .isInstanceOf(ValidationException.class)
                .hasMessage("Text cannot be empty");
    }

    @Test
    @DisplayName("Должен обновлять существующий комментарий")
    void update_shouldUpdateExistingComment() {
        long commentId = 1L;
        String updatedText = "Updated Text";

        CommentDto result = commentService.update(commentId, updatedText);

        assertThat(result.id()).isEqualTo(commentId);
        assertThat(result.text()).isEqualTo(updatedText);

        Optional<CommentDto> updatedComment = commentService.findById(commentId);
        assertThat(updatedComment).isPresent();
        assertThat(updatedComment.get().text()).isEqualTo(updatedText);
    }

    @Test
    @DisplayName("Должен бросать исключение при обновлении комментария с пустым текстом")
    void update_shouldThrowForEmptyText() {
        assertThatThrownBy(() -> commentService.update(1L, ""))
                .isInstanceOf(ValidationException.class)
                .hasMessage("Text cannot be empty");
    }

    @Test
    @DisplayName("Должен бросать исключение при обновлении несуществующего комментария")
    void update_shouldThrowForNonExistingComment() {
        assertThatThrownBy(() -> commentService.update(999L, "Updated Text"))
                .isInstanceOf(EntityNotFoundException.class);
    }


}