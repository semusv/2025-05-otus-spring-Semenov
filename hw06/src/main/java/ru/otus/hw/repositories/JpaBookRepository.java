package ru.otus.hw.repositories;

import jakarta.persistence.EntityGraph;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;
import org.springframework.stereotype.Repository;
import ru.otus.hw.models.Book;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.springframework.data.jpa.repository.EntityGraph.EntityGraphType.FETCH;

@Repository
public class JpaBookRepository implements BookRepository {

    @PersistenceContext
    private EntityManager em;

    private final Class<Book> entityClass;

    public JpaBookRepository() {
        entityClass = Book.class;
    }

    @Override
    public Optional<Book> findById(long id) {
        EntityGraph<?> entityGraph = em.getEntityGraph("book-author-genres-entity-graph");
        Map<String, Object> properties = new HashMap<>();
        properties.put(FETCH.getKey(), entityGraph);

        return Optional.ofNullable(em.find(entityClass, id, properties));
    }

    @Override
    public List<Book> findAll() {
        EntityGraph<?> entityGraph = em.getEntityGraph("book-author-entity-graph");
        TypedQuery<Book> query = em.createQuery(
                "SELECT b FROM Book b",
                entityClass);
        query.setHint(FETCH.getKey(), entityGraph);
        return query.getResultList();
    }

    @Override
    public Book save(Book book) {
        if (book.getId() == 0) {
            em.persist(book);
            return book;
        }
        return em.merge(book);
    }

    @Override
    public void deleteById(long id) {
        try {
            Book proxy = em.getReference(entityClass, id);
            em.remove(proxy);
        } catch (EntityNotFoundException e) {
            throw new EntityNotFoundException("Book with id " + id + " not found");
        }
    }

    @Override
    public boolean notExistsById(long id) {
        Query query = em.createQuery(
                "SELECT COUNT(b) > 0 FROM Book b WHERE b.id = :id"
        );
        query.setParameter("id", id);
        return !(boolean) query.getSingleResult();
    }
}
