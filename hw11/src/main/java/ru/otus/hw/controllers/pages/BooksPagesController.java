package ru.otus.hw.controllers.pages;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import reactor.core.publisher.Mono;

@SuppressWarnings({"SameReturnValue", "unused"})
@Controller
@RequiredArgsConstructor
public class BooksPagesController {

    @GetMapping({"/", "/books"})
    public Mono<String> getBooks() {
        return Mono.just("books-list");
    }

    @GetMapping("/books/{id}")
    public Mono<String> getBook(
            @PathVariable("id") long id) {
        return Mono.just("book-view");
    }

    @GetMapping("/books/{id}/edit")
    public Mono<String> editBook(
            @PathVariable("id") Long id) {
        return Mono.just("book-edit");
    }


    @GetMapping("/books/new")
    public Mono<String> addBook(Model model) {
        return Mono.just("book-edit");
    }


}
