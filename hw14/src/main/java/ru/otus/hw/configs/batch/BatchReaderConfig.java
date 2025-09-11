package ru.otus.hw.configs.batch;

import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.batch.item.database.builder.JdbcCursorItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import ru.otus.hw.models.h2.Author;
import ru.otus.hw.models.h2.Book;
import ru.otus.hw.models.h2.Comment;
import ru.otus.hw.models.h2.Genre;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Configuration
public class BatchReaderConfig {
    private final DataSource dataSource;

    public BatchReaderConfig(DataSource dataSource) {
        this.dataSource = dataSource;
    }



    @Bean
    public JdbcCursorItemReader<Author> authorReader() {
        return new JdbcCursorItemReaderBuilder<Author>()
                .name("authorReader")
                .dataSource(dataSource)
                .sql("SELECT id, full_name FROM authors")
                .rowMapper(new BeanPropertyRowMapper<>(Author.class))
                .build();
    }

    @Bean
    public JdbcCursorItemReader<Book> bookReader() {
        return new JdbcCursorItemReaderBuilder<Book>()
                .name("bookReader")
                .dataSource(dataSource)
                .sql("SELECT b.id, b.title, b.author_id, a.full_name, " +
                     "GROUP_CONCAT(bg.genre_id) as genre_ids " + // Агрегируем ID жанров
                     "FROM books b " +
                     "LEFT JOIN authors a ON b.author_id = a.id " +
                     "LEFT JOIN books_genres bg ON b.id = bg.book_id " +
                     "GROUP BY b.id, b.title, b.author_id, a.full_name " + // Группируем по книге
                     "ORDER BY b.id")
                .rowMapper(BatchReaderConfig::mapBookRow)
                .build();
    }

    private static Book mapBookRow(ResultSet rs, int rowNum) throws SQLException {
        Book book = new Book();
        book.setId(rs.getLong("id"));
        book.setTitle(rs.getString("title"));

        Author author = new Author();
        author.setId(rs.getLong("author_id"));
        author.setFullName(rs.getString("full_name"));
        book.setAuthor(author);

        List<Genre> genres = new ArrayList<>();
        String genreIds = rs.getString("genre_ids");
        if (genreIds != null && !genreIds.isEmpty()) {
            String[] ids = genreIds.split(",");
            for (String id : ids) {
                if (!id.trim().isEmpty()) {
                    Genre genre = new Genre();
                    genre.setId(Long.parseLong(id.trim()));
                    genres.add(genre);
                }
            }
        }
        book.setGenres(genres);
        return book;
    }

    @Bean
    public JdbcCursorItemReader<Genre> genreReader() {
        return new JdbcCursorItemReaderBuilder<Genre>()
                .name("genreReader")
                .dataSource(dataSource)
                .sql("SELECT id, name FROM genres")
                .rowMapper(new BeanPropertyRowMapper<>(Genre.class))
                .build();
    }

    @Bean
    public JdbcCursorItemReader<Map<String, Object>> bookGenresReader() {
        return new JdbcCursorItemReaderBuilder<Map<String, Object>>()
                .name("bookGenresReader")
                .dataSource(dataSource)
                .sql("SELECT book_id, genre_id FROM books_genres")
                .rowMapper((rs, rowNum) -> {
                    Map<String, Object> m = new HashMap<>();
                    m.put("book_id", rs.getLong("book_id"));
                    m.put("genre_id", rs.getLong("genre_id"));
                    return m;
                })
                .build();
    }

    @Bean
    public JdbcCursorItemReader<Comment> commentReader() {
        return new JdbcCursorItemReaderBuilder<Comment>()
                .name("commentReader")
                .dataSource(dataSource)
                .sql("SELECT c.id, c.text, c.book_id " +
                     "FROM comments c ")
                .rowMapper((rs, rowNum) -> {
                    Comment comment = new Comment();
                    comment.setId(rs.getLong("id"));
                    comment.setText(rs.getString("text"));

                    Book book = new Book();
                    book.setId(rs.getLong("book_id"));
                    comment.setBook(book);

                    return comment;
                })
                .build();
    }
}
