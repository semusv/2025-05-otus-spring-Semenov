package ru.otus.hw.controllers.pages;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@SuppressWarnings("SameReturnValue")
@Controller
@RequiredArgsConstructor
public class BooksPagesController {

    @GetMapping({"/", "/books"})
    public String getBooks() {
        return "books-list";
    }

    @GetMapping("/books/{id}")
    public String getBook(
            @PathVariable("id") long id) {
        return "book-view";
    }

    @GetMapping("/books/{id}/edit")
    public String editBook(
            @PathVariable("id") Long id) {
        return "book-edit";
    }


    @GetMapping("/books/new")
    public String addBook(Model model) {
        return "book-edit";
    }


}
