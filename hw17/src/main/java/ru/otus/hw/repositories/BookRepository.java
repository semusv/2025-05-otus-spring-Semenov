package ru.otus.hw.repositories;


import jakarta.annotation.Nonnull;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.rest.core.annotation.RestResource;
import ru.otus.hw.models.Book;

import java.util.List;
import java.util.Optional;

@RepositoryRestResource(path = "book")
public interface BookRepository extends JpaRepository<Book, Long> {
    @EntityGraph("book-author-genres-entity-graph")
    @Override
    @Nonnull
    @RestResource(path = "IDs", rel = "IDs")
    Optional<Book> findById(@Nonnull Long aLong);

    @EntityGraph("book-author-entity-graph")
    @Override
    @Nonnull
    @RestResource(path = "all", rel = "all")
    List<Book> findAll();

}
