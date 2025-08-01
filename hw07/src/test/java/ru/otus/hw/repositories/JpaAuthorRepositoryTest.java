package ru.otus.hw.repositories;

import lombok.val;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import ru.otus.hw.models.Author;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Репозиторий на основе Jpa для работы с авторами ")
@DataJpaTest
class JpaAuthorRepositoryTest {

    private static final int EXPECTED_NUMBER_OF_AUTHORS = 3;
    private static final long NON_EXIST_AUTHOR_ID = 999L;

    @Autowired
    AuthorRepository repositoryJpa;

    @Autowired
    TestEntityManager em;

    @DisplayName("Должен загружать информацию о нужном авторе по его id")
    @Test
    void shouldFindExpectedAuthorById() {
        //given
        val expectedAuthor = em.persist(new Author(0, "Author1"));
        em.detach(expectedAuthor);
        //when
        val optionalActualAuthor = repositoryJpa.findById(expectedAuthor.getId());
        //then
        assertThat(optionalActualAuthor).isPresent().get()
                .usingRecursiveComparison().isEqualTo(expectedAuthor);
    }

    @DisplayName("Должен возвращать пустой Optional, если автор не найден")
    @Test
    void shouldReturnEmptyOptionalWhenAuthorNotFound() {
        //when
        val optionalAuthor = repositoryJpa.findById(NON_EXIST_AUTHOR_ID);
        //then
        assertThat(optionalAuthor).isEmpty();
    }


    @DisplayName("должен загружать список всех авторов")
    @Test
    void shouldReturnCorrectAuthorsList() {
        //given
        em.persist(new Author(0, "Author1"));
        em.persist(new Author(0, "Author2"));
        em.persist(new Author(0, "Author3"));
        em.persist(new Author(0, "Author4"));
        em.flush();
        em.clear();

        //when
        val authors = repositoryJpa.findAll();
        //then
        assertThat(authors).isNotNull().hasSizeGreaterThan(EXPECTED_NUMBER_OF_AUTHORS)
                .allMatch(a -> !a.getFullName().isEmpty());
     }



}