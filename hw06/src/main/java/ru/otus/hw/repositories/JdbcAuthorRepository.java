package ru.otus.hw.repositories;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.stereotype.Repository;
import ru.otus.hw.models.Author;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class JdbcAuthorRepository implements AuthorRepository {

    private final NamedParameterJdbcOperations jdbcOperations;

    @Override
    public List<Author> findAll() {
        // language=SQL
        String sql = """
                SELECT
                    id,
                    full_name
                FROM authors;
                """;
        return jdbcOperations.query(sql,new AuthorRowMapper());
    }

    @Override
    public Optional<Author> findById(long id) {
        var params = new MapSqlParameterSource()
                .addValue("id", id);
        // language=SQL
        String sql = """
                SELECT
                    id,
                    full_name
                FROM authors
                WHERE id = :id;
                """;
       return jdbcOperations.query(sql, params, new AuthorRowMapper()).stream().findFirst();
    }

    private static class AuthorRowMapper implements RowMapper<Author> {
        @Override
        public Author mapRow(ResultSet rs, int i) throws SQLException {
            return new Author(
                    rs.getLong("id"),
                    rs.getString("full_name"));
        }
    }
}
