package ru.otus.hw.repositories;

import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;
import ru.otus.hw.models.Genre;

import java.util.Collection;


public interface GenreRepository extends ReactiveCrudRepository<Genre, Long> {

    @Query("SELECT COUNT(*) FROM genres WHERE id IN (:ids)")
    Mono<Long> countByIdIn(Collection<Long> ids);
}
