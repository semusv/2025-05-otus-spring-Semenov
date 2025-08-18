package ru.otus.hw.repositories;

import io.r2dbc.spi.Row;
import io.r2dbc.spi.RowMetadata;
import lombok.RequiredArgsConstructor;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.otus.hw.models.Author;
import ru.otus.hw.models.Book;
import ru.otus.hw.models.Genre;

import java.util.ArrayList;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class CustomBookRepositoryImpl implements CustomBookRepository {

    private final R2dbcEntityTemplate template;

    @Override
    public Flux<Book> findAll() {
        String sql = """
                SELECT b.*,
                       a.id as a_id, a.full_name as a_name,
                       g.id as g_id, g.name as g_name
                FROM books b
                LEFT JOIN authors a ON b.author_id = a.id
                LEFT JOIN books_genres bg ON b.id = bg.book_id
                LEFT JOIN genres g ON bg.genre_id = g.id
                ORDER BY b.id
                """;

        return template.getDatabaseClient()
                .sql(sql)
                .map(this::mapRowToBook)
                .all()
                .bufferUntilChanged(Book::getId)
                .map(this::mergeBooksFromList);
    }

    @Override
    public Mono<Book> findById(long id) {
        String sql = """
                SELECT b.*,
                       a.id as a_id, a.full_name as a_name,
                       g.id as g_id, g.name as g_name
                FROM books b
                LEFT JOIN authors a ON b.author_id = a.id
                LEFT JOIN books_genres bg ON b.id = bg.book_id
                LEFT JOIN genres g ON bg.genre_id = g.id
                WHERE b.id = :id
                """;

        return template.getDatabaseClient()
                .sql(sql)
                .bind("id", id)
                .map(this::mapRowToBook)
                .all()
                .collectList()
                .flatMap(books -> {
                    if (books.isEmpty()) {
                        return Mono.empty();
                    }
                    return Mono.just(mergeBooksFromList(books));
                });
    }


    @Override
    public Mono<Book> saveBookWithGenres(Book book) {
        boolean isNew = book.getId() == 0;
        Mono<Long> bookOperation = isNew ? insertBook(book) : updateBook(book);
        return bookOperation.flatMap(bookId -> {
            book.setId(bookId);
            Mono<Void> deleteOldGenres = isNew ? Mono.empty()
                    : template.getDatabaseClient()
                    .sql("DELETE FROM books_genres WHERE book_id = :bookId")
                    .bind("bookId", bookId)
                    .fetch()
                    .rowsUpdated()
                    .then();
            Mono<Void> insertGenres = book.getGenres() == null || book.getGenres().isEmpty() ? Mono.empty()
                    : Flux.fromIterable(book.getGenres())
                    .flatMap(genre -> template.getDatabaseClient()
                            .sql("INSERT INTO books_genres (book_id, genre_id) VALUES (:bookId, :genreId)")
                            .bind("bookId", bookId)
                            .bind("genreId", genre.getId())
                            .fetch()
                            .rowsUpdated()
                    ).then();
            return deleteOldGenres.then(insertGenres).thenReturn(book);
        }).flatMap(savedBook -> findById(savedBook.getId()));
    }

    private Mono<Long> insertBook(Book book) {
        return template.getDatabaseClient()
                .sql("INSERT INTO books (title, author_id) VALUES (:title, :author_id)")
                .filter(statement -> statement.returnGeneratedValues("id"))
                .bind("title", book.getTitle())
                .bind("author_id", book.getAuthor().getId())
                .fetch()
                .first()
                .map(row -> (Long) row.get("id"));
    }

    private Mono<Long> updateBook(Book book) {
        return template.getDatabaseClient()
                .sql("UPDATE books SET title = :title, author_id = :author_id WHERE id = :id")
                .bind("title", book.getTitle())
                .bind("author_id", book.getAuthor().getId())
                .bind("id", book.getId())
                .fetch()
                .rowsUpdated()
                .thenReturn(book.getId());
    }

    @SuppressWarnings("DataFlowIssue")
    private Book mapRowToBook(Row row, RowMetadata metadata) {
        if (row == null || row.get("id") == null) {
            return null;
        }
        Book book = new Book();
        book.setId(row.get("id", Long.class));
        book.setTitle(row.get("title", String.class));
        book.setGenres(new ArrayList<>());
        // другие поля книги
        if (row.get("a_id", Long.class) != null) {
            Author author = new Author();
            author.setId(row.get("a_id", Long.class));
            author.setFullName(row.get("a_name", String.class));
            book.setAuthor(author);
        }
        if (row.get("g_id", Long.class) != null) {
            Genre genre = new Genre();
            genre.setId(row.get("g_id", Long.class));
            genre.setName(row.get("g_name", String.class));
            book.getGenres().add(genre);
        }

        return book;
    }

    private Book mergeBooksFromList(List<Book> books) {
        Book mainBook = books.get(0);
        if (books.size() > 1) {
            List<Genre> genres = books.stream()
                    .flatMap(b -> b.getGenres().stream()).toList();
            mainBook.setGenres(genres);
        }
        return mainBook;
    }


}
