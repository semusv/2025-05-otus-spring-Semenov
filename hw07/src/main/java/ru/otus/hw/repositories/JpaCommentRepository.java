package ru.otus.hw.repositories;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;
import org.springframework.stereotype.Repository;
import ru.otus.hw.models.Comment;

import java.util.List;
import java.util.Optional;

@Repository
public class JpaCommentRepository implements CommentRepository {

    @PersistenceContext
    private EntityManager em;

    private final Class<Comment> entityClass;

    public JpaCommentRepository() {
        entityClass = Comment.class;
    }

    @Override
    public Optional<Comment> findById(long id) {
        if (id <= 0) {
            throw new IllegalArgumentException("ID must be positive: " + id);
        }
        return Optional.ofNullable(em.find(entityClass, id));
    }

    @Override
    public List<Comment> findByBookId(long bookId) {
        TypedQuery<Comment> query = em.createQuery(
                "SELECT c FROM Comment  c WHERE c.book.id = :id",
                entityClass);
        query.setParameter("id", bookId);
        return query.getResultList();
    }

    @Override
    public Comment save(Comment comment) {
        if (comment.getId() == 0) {
            em.persist(comment);
            return comment;
        }
        return em.merge(comment);
    }

    @Override
    public void deleteById(long id) {
        try {
            Comment proxy = em.getReference(entityClass, id);
            em.remove(proxy);
        } catch (EntityNotFoundException e) {
            throw new EntityNotFoundException("Comment with id " + id + " not found");
        }
    }

    @Override
    public boolean existsById(long id) {
        Query query = em.createQuery(
                "SELECT COUNT(c) > 0 FROM Comment c WHERE c.id = :id"
        );
        query.setParameter("id", id);
        return (boolean) query.getSingleResult();
    }
}
