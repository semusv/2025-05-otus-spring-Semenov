package ru.otus.hw.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.otus.hw.dto.CommentDto;
import ru.otus.hw.services.CommentService;

@Controller
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @PostMapping("/books/{id}/comments/add")
    public String addCommentToBook(@PathVariable("id") Long id,
                                   @ModelAttribute("comment") CommentDto commentDto) {
        commentService.insert(commentDto);
        return "redirect:/books/" + id + "/display#comments-section";
    }

    @PostMapping("/books/{id}/comments/{commentId}/delete")
    public String deleteComment(@PathVariable("id") Long id,
                                @PathVariable("commentId") Long commentId,
                                RedirectAttributes redirectAttributes) {
        commentService.deleteById(commentId);

        redirectAttributes.addFlashAttribute("message", "Комментарий успешно удален");
        return "redirect:/books/" + id + "/edit#comments-section";
    }
}
