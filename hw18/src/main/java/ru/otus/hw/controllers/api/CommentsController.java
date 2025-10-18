package ru.otus.hw.controllers.api;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.web.server.ResponseStatusException;
import ru.otus.hw.dto.CommentDto;
import ru.otus.hw.services.CommentService;

import java.util.List;

@SuppressWarnings("unused")
@RestController
@RequiredArgsConstructor
@Slf4j
public class CommentsController {

    private final MessageSource messageSource;

    private final CommentService commentService;

    @CircuitBreaker(name = "readOperations", fallbackMethod = "fallbackForGetId")
    @RateLimiter(name = "commentService")
    @GetMapping("/api/books/{id}/comments")
    @ResponseStatus(HttpStatus.OK)
    public List<CommentDto> getCommentsForBookId(
            @PathVariable("id") Long bookId) {
        return commentService.findByBookId(bookId);
    }

    @CircuitBreaker(name = "writeOperations", fallbackMethod = "fallbackForWrite")
    @RateLimiter(name = "commentService")
    @PostMapping("api/books/{id}/comments")
    @ResponseStatus(HttpStatus.CREATED)
    public CommentDto addCommentToBook(
            @PathVariable("id") Long id,
            @Valid @RequestBody CommentDto commentDto) {
        return commentService.insert(commentDto);
    }

    @CircuitBreaker(name = "writeOperations", fallbackMethod = "fallbackForDelete")
    @RateLimiter(name = "commentService")
    @DeleteMapping("api/books/{id}/comments/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCommentFromBook(
            @PathVariable("id") Long id,
            @PathVariable("commentId") Long commentId) {
        commentService.deleteById(commentId);
    }

    public List<CommentDto> fallbackForGetAll(Long id, Exception ex) {
        log.warn("Fallback: returning empty list", ex);
        return List.of();
    }

    public CommentDto fallbackForWrite(CommentDto dto, Exception ex) {
        log.error("Fallback: write operation failed", ex);
        throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Service temporarily unavailable");
    }

    public void fallbackForDelete(Long id, Exception ex) {
        log.error("Fallback: delete operation failed for id {}", id, ex);
        throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Delete operation temporarily unavailable");
    }
}
