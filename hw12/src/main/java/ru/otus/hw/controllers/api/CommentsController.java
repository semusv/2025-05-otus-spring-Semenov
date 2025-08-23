package ru.otus.hw.controllers.api;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import ru.otus.hw.dto.CommentDto;
import ru.otus.hw.services.CommentService;

import java.util.List;

@SuppressWarnings("unused")
@RestController
@RequiredArgsConstructor
public class CommentsController {

    private final MessageSource messageSource;

    private final CommentService commentService;

    @GetMapping("/api/books/{id}/comments")
    public List<CommentDto> getCommentsForBookId(
            @PathVariable("id") Long bookId) {
        return commentService.findByBookId(bookId);
    }

    @PostMapping("api/books/{id}/comments")
    public ResponseEntity<CommentDto> addCommentToBook(
            @PathVariable("id") Long id,
            @Valid @RequestBody CommentDto commentDto) {

        var newCommentDto = commentService.insert(commentDto);
        return ResponseEntity.ok(newCommentDto);
    }

    @DeleteMapping("api/books/{id}/comments/{commentId}")
    public ResponseEntity<Long> deleteCommentFromBook(
            @PathVariable("id") Long id,
            @PathVariable("commentId") Long commentId) {

        commentService.deleteById(commentId);
        return ResponseEntity.ok(commentId);
    }
}
