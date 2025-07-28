package ru.otus.hw.services;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import ru.otus.hw.models.Author;
import ru.otus.hw.models.Book;
import ru.otus.hw.models.Comment;
import ru.otus.hw.models.Genre;

import java.util.List;

@DataMongoTest
public abstract class BaseMongoTest {

    @Autowired
    protected MongoTemplate mongoTemplate;

    @BeforeEach
    void cleanDatabase() {
        mongoTemplate.getDb().drop();
    }

    protected Author createAuthor(String fullName) {
        Author author = new Author(fullName);
        return mongoTemplate.save(author);
    }

    protected Genre createGenre(String name) {
        Genre genre = new Genre(name);
        return mongoTemplate.save(genre);
    }

    protected Book createBook(String title, Author author) {
        Book book = new Book(title, author);
        return mongoTemplate.save(book);
    }

    protected Book createBook(String title, Author author, List<Genre> genres) {
        Book book = new Book(title, author);
        book.setGenres(genres);
        return mongoTemplate.save(book);
    }

    protected Comment createComment(String text, Book book) {
        Comment comment = new Comment(text, book.getId(), book);
        return mongoTemplate.save(comment);
    }
}