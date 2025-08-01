package ru.otus.hw.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import ru.otus.hw.dto.AuthorDto;
import ru.otus.hw.dto.BookDto;
import ru.otus.hw.dto.BookFormDto;
import ru.otus.hw.dto.CommentDto;
import ru.otus.hw.dto.GenreDto;
import ru.otus.hw.services.AuthorService;
import ru.otus.hw.services.BookService;
import ru.otus.hw.services.CommentService;
import ru.otus.hw.services.GenreService;

import java.util.List;

@SuppressWarnings("SameReturnValue")
@Controller
@RequiredArgsConstructor
public class BookController {

    private final BookService bookService;

    private final CommentService commentService;

    private final GenreService genreService;

    private final AuthorService authorService;


    @GetMapping({"/", "/books"})
    public String getBooks(Model model) {

        var books = bookService.findAll();
        model.addAttribute("books", books);
        return "books-list";
    }

    @GetMapping("/books/{id}/display")
    public String getBook(
            @PathVariable("id") Long id,
            Model model) {

        var book = bookService.findById(id);
        model.addAttribute("book", book);

        var comments = commentService.findByBookId(id);
        model.addAttribute("comments", comments);

        return "book-view";
    }

    @GetMapping("/books/{id}/edit")
    public String editBook(
            @PathVariable("id") Long id,
            Model model) {

        BookDto bookDto = bookService.findById(id);
        BookFormDto bookFormDto = new BookFormDto(
                bookDto.id(),
                bookDto.title(),
                bookDto.author().id(),
                bookDto.genres().stream().map(GenreDto::id).toList()
        );

        List<CommentDto> commentsDto = commentService.findByBookId(id);
        List<GenreDto> genresDto = genreService.findAll();
        List<AuthorDto> authorsDto = authorService.findAll();

        model.addAttribute("book", bookFormDto);
        model.addAttribute("comments", commentsDto);
        model.addAttribute("allGenres", genresDto);
        model.addAttribute("allAuthors", authorsDto);

        return "book-edit";
    }

    @PostMapping("/books/{id}/update")
    public String updateBook(
            @PathVariable("id") Long id,
            @ModelAttribute("book") BookFormDto bookFormDto) {
        bookService.update(bookFormDto);
        return "redirect:/books/" + id + "/display";
    }

    @PostMapping("/books/{id}/delete")
    public String deleteBook(
            @PathVariable("id") Long id) {
        bookService.deleteById(id);
        return "redirect:/";
    }


    @GetMapping("/books/new")
    public String addBook(Model model) {

        BookFormDto bookFormDto = BookFormDto.builder().genreIds(List.of()).build();
        List<GenreDto> genresDto = genreService.findAll();
        List<AuthorDto> authorsDto = authorService.findAll();

        model.addAttribute("book", bookFormDto);
        model.addAttribute("allGenres", genresDto);
        model.addAttribute("allAuthors", authorsDto);

        return "book-edit";
    }

    @PostMapping("/books/create")
    public String createBook(
            @ModelAttribute("book") BookFormDto bookFormDto) {
        var newId = bookService.insert(bookFormDto).id();
        return "redirect:/books/" + newId + "/display";
    }

}
