package ru.otus.hw.mongock.changelog;

import com.github.cloudyrock.mongock.ChangeLog;
import com.github.cloudyrock.mongock.ChangeSet;
import com.mongodb.client.MongoDatabase;
import ru.otus.hw.models.Author;
import ru.otus.hw.models.Book;
import ru.otus.hw.models.Comment;
import ru.otus.hw.models.Genre;
import ru.otus.hw.repositories.AuthorRepository;
import ru.otus.hw.repositories.BookRepository;
import ru.otus.hw.repositories.CommentRepository;
import ru.otus.hw.repositories.GenreRepository;

import java.util.List;

@ChangeLog
public class DatabaseChangelog {

    @ChangeSet(order = "001", id = "dropDb", author = "vvsemenov", runAlways = true)
    public void dropDb(MongoDatabase db) {
        db.drop();
    }

    @ChangeSet(order = "002", id = "init-authors", author = "vvsemenov")
    public void initAuthors(AuthorRepository authorRepository) {
        authorRepository.save(new Author("Author_1"));
        authorRepository.save(new Author("Author_2"));
        authorRepository.save(new Author("Author_3"));
        authorRepository.save(new Author("Author_4"));
    }

    @ChangeSet(order = "003", id = "init-genres", author = "vvsemenov")
    public void initGenres(GenreRepository genreRepository) {
        genreRepository.save(new Genre("Genre_1"));
        genreRepository.save(new Genre("Genre_2"));
        genreRepository.save(new Genre("Genre_3"));
        genreRepository.save(new Genre("Genre_4"));
        genreRepository.save(new Genre("Genre_5"));
        genreRepository.save(new Genre("Genre_6"));
    }

    @ChangeSet(order = "004", id = "init-books", author = "vvsemenov")
    public void initBooks(
            BookRepository bookRepository,
            AuthorRepository authorRepository,
            GenreRepository genreRepository
    ) {
        List<Author> authors = authorRepository.findAll();
        List<Genre> genres = genreRepository.findAll();

        Book book1 = new Book("BookTitle_1", authors.get(0));
        book1.setGenres(List.of(genres.get(0), genres.get(1)));

        Book book2 = new Book("BookTitle_2", authors.get(1));
        book2.setGenres(List.of(genres.get(2), genres.get(3)));

        Book book3 = new Book("BookTitle_3", authors.get(2));
        book3.setGenres(List.of(genres.get(4), genres.get(5)));

        bookRepository.saveAll(List.of(book1, book2, book3));
    }

    @ChangeSet(order = "005", id = "init-comments", author = "vvsemenov")
    public void initComments(CommentRepository commentRepository, BookRepository bookRepository) {
        List<Book> books = bookRepository.findAll();

        commentRepository.saveAll(List.of(
                new Comment("Коммент 1 // книги 1", books.get(0).getId(), books.get(0)),
                new Comment("Коммент 2 // книги 1", books.get(0).getId(), books.get(0)),
                new Comment("Коммент 3 // книги 1", books.get(0).getId(), books.get(0)),
                new Comment("Коммент 1 // книги 2", books.get(1).getId(), books.get(1))
        ));
    }

}
