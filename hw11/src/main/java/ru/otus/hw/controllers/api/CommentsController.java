package ru.otus.hw.controllers.api;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.otus.hw.dto.CommentDto;
import ru.otus.hw.services.CommentService;

@SuppressWarnings("unused")
@RestController
@RequiredArgsConstructor
public class CommentsController {

    private final CommentService commentService;

    @GetMapping("/api/books/{id}/comments")
    @ResponseStatus(HttpStatus.OK)
    public Flux<CommentDto> getCommentsForBookId(
            @PathVariable("id") Long bookId) {
        return commentService.findByBookId(bookId);
    }

    @PostMapping("api/books/{id}/comments")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<CommentDto> addCommentToBook(
            @PathVariable("id") Long bookId,
            @Valid @RequestBody CommentDto requestDto) {
        return commentService.insert(requestDto);
    }

    @DeleteMapping("api/books/{id}/comments/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> deleteCommentFromBook(
            @PathVariable("id") Long id,
            @PathVariable("commentId") Long commentId) {
       return commentService.deleteById(commentId);
    }
}
