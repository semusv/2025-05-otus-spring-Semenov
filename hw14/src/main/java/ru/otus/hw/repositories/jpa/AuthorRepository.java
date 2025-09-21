package ru.otus.hw.repositories.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.otus.hw.models.h2.Author;


public interface AuthorRepository extends JpaRepository<Author, Long> {
}
