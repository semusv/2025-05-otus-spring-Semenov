package ru.otus.hw.repositories;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;
import ru.otus.hw.models.Genre;

import java.util.List;
import java.util.Set;

@Repository
@ConditionalOnProperty(value = "repository.type", havingValue = "jpa", matchIfMissing = true)
public class JpaGenreRepository implements GenreRepository {

    @PersistenceContext
    private EntityManager em;

    private final Class<Genre> entityClass;

    public JpaGenreRepository() {
        entityClass = Genre.class;
    }


    @Override
    public List<Genre> findAll() {
        return em.createQuery("SELECT g FROM Genre  g", entityClass)
                .getResultList();
    }

    @Override
    public List<Genre> findAllByIds(Set<Long> ids) {
        TypedQuery<Genre> query = em.createQuery(
                "SELECT g FROM Genre g WHERE g.id in :ids",
                entityClass);
        query.setParameter("ids", ids);
        return query.getResultList();
    }
}
