package ru.otus.hw.batch;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.shell.boot.StandardCommandsAutoConfiguration;
import ru.otus.hw.models.mongo.MongoAuthor;
import ru.otus.hw.models.mongo.MongoBook;
import ru.otus.hw.models.mongo.MongoComment;
import ru.otus.hw.models.mongo.MongoGenre;

import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest
@SpringBatchTest
@ImportAutoConfiguration(exclude = StandardCommandsAutoConfiguration.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class BatchMigrationTest {

    @Autowired
    protected JobLauncherTestUtils jobLauncherTestUtils;

    @Autowired
    protected MongoTemplate mongoTemplate;

    protected static boolean migrationExecuted = false;

    @BeforeAll
    void runMigrationOnce() throws Exception {
        if (!migrationExecuted) {
            JobParametersBuilder jobParametersBuilder = new JobParametersBuilder();
            jobParametersBuilder.addLong("startAt", System.currentTimeMillis());
            JobParameters parameters = jobParametersBuilder.toJobParameters();

            JobExecution execution = jobLauncherTestUtils.launchJob(parameters);
            assertThat(execution.getStatus()).isEqualTo(BatchStatus.COMPLETED);

            migrationExecuted = true;
        }
    }

    @AfterAll
    void cleanup() {
        mongoTemplate.dropCollection("authors");
        mongoTemplate.dropCollection("books");
        mongoTemplate.dropCollection("genres");
        mongoTemplate.dropCollection("comments");
    }

    @Test
    @DisplayName("Проверяет миграцию авторов")
    void testAuthorsMigration() {

        List<MongoAuthor> authors = mongoTemplate.findAll(MongoAuthor.class, "authors");
        Assertions.assertThat(authors).hasSize(3);

        MongoAuthor authors1 = mongoTemplate.findById("1", MongoAuthor.class, "authors");
        org.junit.jupiter.api.Assertions.assertNotNull(authors1);
        Assertions.assertThat(authors1.getFullName()).isEqualTo("Author_1");
        Assertions.assertThat(authors1.getId()).isEqualTo("1");
    }

    @Test
    @DisplayName("Проверяет все авторов на корректность данных")
    void testAllAuthorsData() {
        List<MongoAuthor> authors = mongoTemplate.findAll(MongoAuthor.class, "authors");


        Assertions.assertThat(authors)
                .extracting(MongoAuthor::getFullName)
                .containsExactlyInAnyOrder("Author_1", "Author_2", "Author_3");
    }


    @Test
    @DisplayName("Проверяет миграцию книг")
    void testBooksMigration() {
        List<MongoBook> books = mongoTemplate.findAll(MongoBook.class, "books");
        Assertions.assertThat(books).hasSize(3);
    }

    @Test
    @DisplayName("Проверяет данные конкретной книги")
    void testBookData() {
        MongoBook book1 = mongoTemplate.findById("1", MongoBook.class, "books");
        org.junit.jupiter.api.Assertions.assertNotNull(book1);
        Assertions.assertThat(book1.getTitle()).isEqualTo("BookTitle_1");
        Assertions.assertThat(book1.getAuthor().getId()).isEqualTo("1");
        Assertions.assertThat(book1.getGenres()).hasSize(2);
    }

    @Test
    @DisplayName("Проверяет связи книг с авторами")
    void testBookAuthorRelations() {
        List<MongoBook> books = mongoTemplate.findAll(MongoBook.class, "books");

        Assertions.assertThat(books)
                .isNotNull()
                .isNotEmpty()
                .allMatch(book -> book.getAuthor() != null && book.getAuthor().getId() != null);
    }

    @Test
    @DisplayName("Проверяет интеграционные связи между сущностями")
    void testEntityRelationships() {

        MongoBook book = mongoTemplate.findById("1", MongoBook.class, "books");
        org.junit.jupiter.api.Assertions.assertNotNull(book);
        MongoAuthor author = mongoTemplate.findById(book.getAuthor().getId(), MongoAuthor.class, "authors");

        Assertions.assertThat(author).isNotNull();
        Assertions.assertThat(author.getId()).isEqualTo(book.getAuthor().getId());


        Assertions.assertThat(book.getGenres()).isNotEmpty();

        MongoGenre genre = mongoTemplate.findById(book.getGenres().get(0).getId(), MongoGenre.class, "genres");
        Assertions.assertThat(genre).isNotNull();
    }

    @Test
    @DisplayName("Проверяет миграцию комментариев")
    void testCommentsMigration() {
        List<MongoComment> comments = mongoTemplate.findAll(MongoComment.class, "comments");
        Assertions.assertThat(comments).hasSize(4);
    }

    @Test
    @DisplayName("Проверяет наличие конкретного комментария")
    void testSpecificComment() {
        List<MongoComment> comments = mongoTemplate.findAll(MongoComment.class, "comments");

        Assertions.assertThat(comments.stream()
                        .anyMatch(c -> "Коммент 1 // книги 1".equals(c.getText())))
                .isTrue();
    }

    @Test
    @DisplayName("Проверяет структуру комментариев")
    void testCommentsStructure() {
        List<MongoComment> comments = mongoTemplate.findAll(MongoComment.class, "comments");


        Assertions.assertThat(comments)
                .isNotNull()
                .isNotEmpty()
                .allMatch(comment -> comment.getText() != null && !comment.getText().isEmpty());
    }

    @Test
    @DisplayName("Проверяет миграцию жанров")
    void testGenresMigration() {
        List<MongoGenre> genres = mongoTemplate.findAll(MongoGenre.class, "genres");
        Assertions.assertThat(genres).hasSize(6);
    }

    @Test
    @DisplayName("Проверяет данные жанров")
    void testGenreData() {
        MongoGenre genre = mongoTemplate.findById("1", MongoGenre.class, "genres");
        org.junit.jupiter.api.Assertions.assertNotNull(genre);
        Assertions.assertThat(genre.getName()).isEqualTo("Genre_1");
    }
}
