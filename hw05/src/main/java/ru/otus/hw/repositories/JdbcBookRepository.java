package ru.otus.hw.repositories;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;
import ru.otus.hw.exceptions.EntityNotFoundException;
import ru.otus.hw.models.Author;
import ru.otus.hw.models.Book;
import ru.otus.hw.models.Genre;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class JdbcBookRepository implements BookRepository {

    private static final String BOOK_ID = "book_id";

    private static final String GENRE_ID = "genre_id";

    private static final String TITLE = "title";

    private static final String AUTHOR_ID = "author_id";

    private static final String FULL_NAME = "full_name";

    private static final String GENRE_NAME = "genre_name";

    private final GenreRepository genreRepository;

    private final NamedParameterJdbcOperations jdbc;

    @Override
    public Optional<Book> findById(long id) {
        var params = new MapSqlParameterSource()
                .addValue(BOOK_ID, id);
        //language=sql
        String sql = """
                SELECT
                    b.id as book_id,
                    b.title,
                    a.id as author_id,
                    a.full_name,
                    g.id as genre_id,
                    g.name as genre_name
                FROM books b
                LEFT JOIN authors a ON b.author_id = a.id
                LEFT JOIN books_genres bg ON b.id = bg.book_id
                LEFT JOIN genres g ON bg.genre_id = g.id
                WHERE b.id = :book_id
                ORDER BY b.id
                """;
        Book book = jdbc.query(sql, params, new BookResultSetExtractor());
        return Optional.ofNullable(book);
    }

    @Override
    public List<Book> findAll() {
        var genres = genreRepository.findAll();
        var relations = getAllGenreRelations();
        var books = getAllBooksWithoutGenres();
        mergeBooksInfo(books, genres, relations);
        return books;
    }

    @Override
    public Book save(Book book) {
        if (book.getId() == 0) {
            return insert(book);
        }
        return update(book);
    }

    @Override
    public void deleteById(long id) {
        var params = new MapSqlParameterSource()
                .addValue(BOOK_ID, id);
        // language=SQL
        String sql = """
                DELETE FROM books
                WHERE id = :book_id;
                """;
        jdbc.update(sql, params);
    }

    private List<Book> getAllBooksWithoutGenres() {
        // language=SQL
        String sql = """
                SELECT
                    books.id as book_id,
                    books.title,
                    books.author_id,
                    authors.full_name
                FROM books
                INNER JOIN authors
                    on  books.author_id = authors.id;
                """;
        return jdbc.query(sql, new BookRowMapper());
    }

    private List<BookGenreRelation> getAllGenreRelations() {
        // language=SQL
        String sql = """
                SELECT book_id, genre_id
                FROM books_genres;
                """;
        return jdbc.query(sql, (rs, rowNum) ->
                new BookGenreRelation(
                        rs.getLong(BOOK_ID),
                        rs.getLong(GENRE_ID)));
    }

    private void mergeBooksInfo(List<Book> booksWithoutGenres, List<Genre> genres,
                                List<BookGenreRelation> relations) {

        // Создаем Map<genreId, Genre> для быстрого поиска
        Map<Long, Genre> genreMap = genres.stream()
                .collect(Collectors.toMap(Genre::getId, Function.identity()));

        // Группируем genreIds по bookId
        Map<Long, List<Long>> genreIdsByBookId = relations.stream()
                .collect(Collectors.groupingBy(
                        BookGenreRelation::bookId,
                        Collectors.mapping(BookGenreRelation::genreId, Collectors.toList())
                ));

        for (Book book : booksWithoutGenres) {
            List<Genre> genresForBooks = genreIdsByBookId.getOrDefault(book.getId(), List.of()).stream()
                    .map(genreMap::get)
                    .filter(Objects::nonNull)
                    .toList();
            book.setGenres(genresForBooks);
        }
    }

    private Genre findGenreById(List<Genre> genres, long genreId) {
        return genres.stream()
                .filter(genre -> genre.getId() == genreId)
                .findFirst()
                .orElse(null);
    }


    private Book insert(Book book) {
        var keyHolder = new GeneratedKeyHolder();
        var params = new MapSqlParameterSource()
                .addValue(TITLE, book.getTitle())
                .addValue(AUTHOR_ID, book.getAuthor().getId());
        // language=SQL
        String sql = """
                INSERT INTO books ( title, author_id)
                    VALUES ( :title, :author_id);
                """;
        jdbc.update(sql, params, keyHolder);
        //noinspection DataFlowIssue
        book.setId(keyHolder.getKeyAs(Long.class));
        batchInsertGenresRelationsFor(book);
        return book;
    }

    private Book update(Book book) {
        var params = new MapSqlParameterSource()
                .addValue(BOOK_ID, book.getId())
                .addValue(TITLE, book.getTitle())
                .addValue(AUTHOR_ID, book.getAuthor().getId());

        //language=sql
        String sql = """
                    UPDATE books
                        SET title = :title, author_id = :author_id
                        WHERE id = :book_id
                """;

        int rowsUpdated = jdbc.update(sql, params);
        if (rowsUpdated <= 0) {
            throw new EntityNotFoundException("Book row not updated for id=%d".formatted(book.getId()));
        }
        removeGenresRelationsFor(book);
        batchInsertGenresRelationsFor(book);
        return book;
    }

    private void batchInsertGenresRelationsFor(Book book) {
        List<Genre> genres = book.getGenres();
        if (genres.isEmpty()) {
            return; // если жанров нет, ничего не делаем
        }
        var batchArgs = genres.stream()
                .map(genre -> new MapSqlParameterSource()
                        .addValue(BOOK_ID, book.getId())
                        .addValue(GENRE_ID, genre.getId()))
                .toArray(SqlParameterSource[]::new);
        // language=sql
        String sql = """
                INSERT INTO books_genres (book_id, genre_id)
                    VALUES (:book_id, :genre_id)
                """;
        jdbc.batchUpdate(sql, batchArgs);
    }

    private void removeGenresRelationsFor(Book book) {
        var params = new MapSqlParameterSource()
                .addValue(BOOK_ID, book.getId());
        //language=sql
        String sql = """
                    DELETE FROM books_genres
                        where book_id = :book_id;
                """;
        jdbc.update(sql, params);
    }

    private static class BookRowMapper implements RowMapper<Book> {

        @Override
        public Book mapRow(ResultSet rs, int rowNum) throws SQLException {
            Book bookWithoutGenres = new Book();
            bookWithoutGenres.setId(rs.getLong(BOOK_ID));
            bookWithoutGenres.setTitle(rs.getString(TITLE));
            bookWithoutGenres.setAuthor(new Author(
                    rs.getLong(AUTHOR_ID),
                    rs.getString("full_name")));
            return bookWithoutGenres;
        }
    }

    //@SuppressWarnings("ClassCanBeRecord")
    @RequiredArgsConstructor
    private static class BookResultSetExtractor implements ResultSetExtractor<Book> {

        @Override
        public Book extractData(ResultSet rs) throws SQLException, DataAccessException {
            Book book = null;
            while (rs.next()) {
                long bookId = rs.getLong(BOOK_ID);
                if (book == null) {
                    book = new Book();
                    book.setId(bookId);
                    book.setTitle(rs.getString(TITLE));
                    book.setAuthor(new Author(
                            rs.getLong(AUTHOR_ID),
                            rs.getString(FULL_NAME)
                    ));
                    book.setGenres(new ArrayList<>());
                }
                if (rs.getObject(GENRE_ID) != null) {
                    book.getGenres().add(new Genre(
                            rs.getLong(GENRE_ID),
                            rs.getString(GENRE_NAME)));
                }
            }
            return book;
        }
    }

    private record BookGenreRelation(long bookId, long genreId) {
    }
}
