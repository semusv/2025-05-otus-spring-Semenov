package ru.otus.hw.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.otus.hw.dto.AuthorDto;
import ru.otus.hw.dto.BookCreateDto;
import ru.otus.hw.dto.BookUpdateDto;
import ru.otus.hw.dto.CommentDto;
import ru.otus.hw.dto.GenreDto;
import ru.otus.hw.mappers.BookMapper;
import ru.otus.hw.services.AuthorService;
import ru.otus.hw.services.BookService;
import ru.otus.hw.services.CommentService;
import ru.otus.hw.services.GenreService;

import java.util.List;

@SuppressWarnings("SameReturnValue")
@Controller
@RequiredArgsConstructor
@Slf4j
public class BookController {

    private final BookService bookService;

    private final CommentService commentService;

    private final GenreService genreService;

    private final AuthorService authorService;

    private final MessageSource messageSource;

    private final BookMapper bookMapper;

    @GetMapping({"/", "/books"})
    public String getBooks(Model model) {

        var books = bookService.findAll();
        model.addAttribute("books", books);
        return "books-list";
    }

    @GetMapping("/books/{id}")
    public String getBook(
            @PathVariable("id") Long id,
            Model model) {

        var bookDto = bookService.findById(id);
        model.addAttribute("book",
                bookMapper.toBookViewDto(
                        bookDto,
                        commentService.findByBookId(id)));
        return "book-view";

    }

    @GetMapping("/books/{id}/edit")
    public String editBook(
            @PathVariable("id") Long id,
            Model model) {
        // Не перезаписываем атрибуты, если они пришли из flash
        if (!model.containsAttribute("book")) {
            model.addAttribute(
                    "book",
                    bookMapper.toBookUpdateDto(
                            bookService.findById(id)
                            ));
        }
        List<GenreDto> genresDto = genreService.findAll();
        List<AuthorDto> authorsDto = authorService.findAll();
        List<CommentDto> commentsDto = commentService.findByBookId(id);

        model.addAttribute("comments", commentsDto);
        model.addAttribute("allGenres", genresDto);
        model.addAttribute("allAuthors", authorsDto);
        return "book-edit";
    }


    @PostMapping("/books/{id}/update")
    public String updateBook(
            @PathVariable("id") Long id,
            @Valid @ModelAttribute("book") BookUpdateDto bookUpdateDto,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("book", bookUpdateDto);
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.book", bindingResult);
            return "redirect:/books/" + id + "/edit";
        }
        bookService.update(bookUpdateDto);
        String msgSuccess = messageSource.getMessage(
                "api.response.ok.save.book",
                new Object[]{bookUpdateDto.id()},
                LocaleContextHolder.getLocale());
        redirectAttributes.addFlashAttribute(
                "successMessage",
                msgSuccess);
        return "redirect:/books/" + id + "/edit";
    }

    @PostMapping("/books/{id}/delete")
    public String deleteBook(
            @PathVariable("id") Long id) {
        bookService.deleteById(id);
        return "redirect:/";
    }

    @GetMapping("/books/new")
    public String addBook(Model model) {
        BookCreateDto bookCreateDto = BookCreateDto.builder().genreIds(List.of()).build();
        List<GenreDto> genresDto = genreService.findAll();
        List<AuthorDto> authorsDto = authorService.findAll();

        model.addAttribute("book", bookCreateDto);
        model.addAttribute("allGenres", genresDto);
        model.addAttribute("allAuthors", authorsDto);
        return "book-edit";
    }

    @PostMapping("/books/create")
    public String createBook(
            @ModelAttribute("book") BookCreateDto bookCreateDto) {
        var newId = bookService.insert(bookCreateDto).id();
        return "redirect:/books/" + newId;
    }

}
