package ru.otus.hw.models;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DocumentReference;

import java.util.List;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "books")
public class Book {
    @Id
    private String id;

    private String title;

    private Author author;

    private List<Genre> genres;

    @DocumentReference(lazy = true)
    @Transient
    private List<Comment> comments;

    public Book(String title, Author author) {
        this.title = title;
        this.author = author;
    }
}
