package ru.otus.hw.commands;

import lombok.RequiredArgsConstructor;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;
import ru.otus.hw.mappers.CommentMapper;
import ru.otus.hw.services.CommentService;

import java.util.stream.Collectors;

@RequiredArgsConstructor
@ShellComponent
@SuppressWarnings({"unused"})
public class CommentCommands {
    private final CommentService commentService;

    private final CommentMapper commentDtoConverter;

    // cbid 1
    @ShellMethod(value = "Find comment by id", key = "cbid")
    public String findCommentById(@ShellOption(value = "cid") String id) {
        return commentService.findById(id)
                .map(commentDtoConverter::dtoToString)
                .orElse("Comment with id %s not found".formatted(id));
    }

    // cbbid 1
    @ShellMethod(value = "Find comments by Book_Id", key = "cbbid")
    public String findCommentsByBookId(@ShellOption(value = "bid") String bookId) {
        return commentService.findByBookId(bookId).stream()
                .map(commentDtoConverter::dtoToString)
                .collect(Collectors.joining("," + System.lineSeparator()));
    }

    // cins newComment 1
    @ShellMethod(value = "Insert comment", key = "cins")
    public String insertComment(@ShellOption(value = "text") String text,
                                @ShellOption(value = "bid") String bookId) {
        var savedComment = commentService.insert(text, bookId);
        return commentDtoConverter.dtoToString(savedComment);
    }

    // cupd 1 editedComment 1
    @ShellMethod(value = "Update comment", key = "cupd")
    public String updateComment(@ShellOption(value = "cid") String id,
                                @ShellOption(value = "text") String title) {
        var savedComment = commentService.update(id, title);
        return commentDtoConverter.dtoToString(savedComment);
    }

    // cdel 1
    @ShellMethod(value = "Delete comment by id", key = "cdel")
    public void deleteComment(@ShellOption(value = "id") String id) {
        commentService.deleteById(id);
    }
}

