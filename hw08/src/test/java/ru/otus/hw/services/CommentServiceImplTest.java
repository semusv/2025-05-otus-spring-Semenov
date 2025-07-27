package ru.otus.hw.services;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import ru.otus.hw.dto.CommentDto;
import ru.otus.hw.mappers.AuthorMapper;
import ru.otus.hw.mappers.CommentMapper;
import ru.otus.hw.models.Author;
import ru.otus.hw.models.Book;
import ru.otus.hw.models.Comment;
import ru.otus.hw.services.validators.CommentValidatorImpl;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@Import({CommentServiceImpl.class,
        CommentMapper.class,
        AuthorMapper.class,
        CommentValidatorImpl.class})
class CommentServiceImplTest extends BaseMongoTest {

    private final CommentService commentService;

    @Autowired
    public CommentServiceImplTest(CommentService commentService) {
        this.commentService = commentService;
    }

    @Test
    @DisplayName("Должен находить комментарий по ID")
    void shouldFindCommentById() {
        // given
        Author author = createAuthor("Test Author");
        Book book = createBook("Test Book", author);
        Comment comment = createComment("Test comment", book);

        // when
        Optional<CommentDto> result = commentService.findById(comment.getId());

        // then
        assertThat(result).isPresent();
        assertAll(
                () -> assertThat(result.get())
                        .extracting(CommentDto::text, CommentDto::bookId)
                        .containsExactly("Test comment", book.getId())
        );
    }

    @Test
    @DisplayName("Должен находить все комментарии для книги")
    void shouldFindCommentsByBookId() {
        // given
        Author author = createAuthor("Test Author");
        Book book1 = createBook("Book 1", author);
        Book book2 = createBook("Book 2", author);

        createComment("Comment 1", book1);
        createComment("Comment 2", book1);
        createComment("Comment 3", book2);

        // when
        List<CommentDto> result = commentService.findByBookId(book1.getId());

        // then
        assertAll(
                () -> assertThat(result).hasSize(2),
                () -> assertThat(result)
                        .extracting(CommentDto::text)
                        .containsExactlyInAnyOrder("Comment 1", "Comment 2"),
                () -> assertThat(result)
                        .allMatch(dto -> dto.bookId().equals(book1.getId()))
        );
    }

    @Test
    @DisplayName("Должен создавать новый комментарий с валидной ссылкой на книгу")
    void shouldCreateNewCommentWithBookReference() {
        // given
        Author author = createAuthor("Test Author");
        Book book = createBook("Test Book", author);

        // when
        CommentDto result = commentService.insert("New comment", book.getId());

        // then
        Comment savedComment = mongoTemplate.findById(result.id(), Comment.class);
        assertThat(result.text()).isEqualTo("New comment");
        assertThat(result.bookId()).isEqualTo(book.getId());
        assertThat(savedComment).isNotNull();
        assertThat(savedComment.getBookId()).isEqualTo(book.getId());
    }

    @Test
    @DisplayName("Должен обновлять текст комментария")
    void shouldUpdateCommentText() {
        // given
        Author author = createAuthor("Test Author");
        Book book = createBook("Test Book", author);
        Comment comment = createComment("Old text", book);

        // when
        CommentDto result = commentService.update(comment.getId(), "Updated text");

        // then
        Comment updatedComment = mongoTemplate.findById(comment.getId(), Comment.class);
        assertThat(result.text()).isEqualTo("Updated text");
        assertThat(updatedComment).isNotNull();
        assertThat(updatedComment.getText()).isEqualTo("Updated text");
        assertThat(updatedComment.getBookId()).isEqualTo(book.getId());

    }

    @Test
    @DisplayName("Должен удалять комментарий")
    void shouldDeleteComment() {
        // given
        Author author = createAuthor("Test Author");
        Book book1 = createBook("Book 1", author);
        Book book2 = createBook("Book 2", author);

        Comment comment1 = createComment("Comment 1", book1);
        createComment("Comment 2", book1);
        createComment("Comment 3", book2);

        // when
        commentService.deleteById(comment1.getId());

        // then
        List<Comment> comments = mongoTemplate.findAll(Comment.class);
        assertThat(mongoTemplate.findById(comment1.getId(), Comment.class)).isNull();
        assertThat(comments).hasSize(2);
    }
}