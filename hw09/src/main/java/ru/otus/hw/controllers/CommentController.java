package ru.otus.hw.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.otus.hw.dto.CommentDto;
import ru.otus.hw.services.CommentService;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    private final MessageSource messageSource;

    @PostMapping("/books/{id}/comments/add")
    public String addCommentToBook(
            @PathVariable("id") Long id,
            @Valid @ModelAttribute("comment") CommentDto commentDto,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            List<String> errorMessages = bindingResult.getAllErrors()
                    .stream()
                    .map(error -> messageSource.getMessage(error, LocaleContextHolder.getLocale()))
                    .toList();
            redirectAttributes.addFlashAttribute("errorMessages", errorMessages);
            redirectAttributes.addFlashAttribute("comment", commentDto);
        } else {
            commentService.insert(commentDto);
            var successMsg = messageSource.getMessage(
                    "api.response.ok.save.comment",
                    new Object[]{id},
                    LocaleContextHolder.getLocale());

            redirectAttributes.addFlashAttribute("successMessage", successMsg);
        }
        return "redirect:/books/" + id + "#comments-section";
    }

    @PostMapping("/books/{id}/comments/{commentId}/delete")
    public String deleteComment(@PathVariable("id") Long id,
                                @PathVariable("commentId") Long commentId,
                                RedirectAttributes redirectAttributes) {
        commentService.deleteById(commentId);

        String successMsg = messageSource.getMessage("comments.success.deleted", null,
                LocaleContextHolder.getLocale());

        redirectAttributes.addFlashAttribute("message", successMsg);
        return "redirect:/books/" + id + "/edit#comments-section";
    }
}
