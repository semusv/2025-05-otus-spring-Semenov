package ru.otus.hw.repositories.jpa;


import jakarta.annotation.Nonnull;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.otus.hw.models.h2.Book;

import java.util.List;
import java.util.Optional;

public interface BookRepository extends JpaRepository<Book, Long> {
    @EntityGraph("book-author-genres-entity-graph")
    @Override
    @Nonnull
    Optional<Book> findById(@Nonnull Long aLong);

    @EntityGraph("book-author-entity-graph")
    @Override
    @Nonnull
    List<Book> findAll();

}
