package ru.otus.hw.repositories;


import jakarta.annotation.Nonnull;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.otus.hw.models.Book;

import java.util.List;
import java.util.Optional;

public interface BookRepository extends JpaRepository<Book, Long> {
    @EntityGraph(attributePaths = {"author", "genres"})
    @Override
    @Nonnull
    Optional<Book> findById(@Nonnull Long aLong);

    @EntityGraph(attributePaths = {"author"})
    @Override
    @Nonnull
    List<Book> findAll();

    @Override
    boolean existsById(@Nonnull Long aLong);
}
