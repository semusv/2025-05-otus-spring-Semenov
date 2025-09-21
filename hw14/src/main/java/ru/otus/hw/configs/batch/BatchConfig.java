package ru.otus.hw.configs.batch;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.ChunkListener;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.FlowBuilder;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.job.flow.Flow;
import org.springframework.batch.core.job.flow.support.SimpleFlow;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.data.MongoItemWriter;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.lang.NonNull;
import org.springframework.transaction.PlatformTransactionManager;
import ru.otus.hw.models.h2.Author;
import ru.otus.hw.models.h2.Book;
import ru.otus.hw.models.h2.Comment;
import ru.otus.hw.models.h2.Genre;
import ru.otus.hw.models.mongo.MongoAuthor;
import ru.otus.hw.models.mongo.MongoBook;
import ru.otus.hw.models.mongo.MongoComment;
import ru.otus.hw.models.mongo.MongoGenre;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Configuration
@Slf4j
@AllArgsConstructor
public class BatchConfig {

    private final JobRepository jobRepository;

    private final PlatformTransactionManager transactionManager;


    /* ---------- Кэши ---------- */
    private final Map<Long, MongoGenre> genreCache = new ConcurrentHashMap<>();


    /* ---------- Processors ---------- */
    @Bean
    public ItemProcessor<Author, MongoAuthor> authorProcessor() {
        return author -> {
            MongoAuthor ma = new MongoAuthor(author.getFullName());
            ma.setId(String.valueOf(author.getId()));
            return ma;
        };
    }

    @Bean
    public ItemProcessor<Genre, MongoGenre> genreProcessor() {
        return genre -> {
            MongoGenre mg = new MongoGenre(genre.getName());
            mg.setId(String.valueOf(genre.getId()));
            genreCache.put(genre.getId(), mg);
            return mg;
        };
    }


    @Bean
    public ItemProcessor<Book, MongoBook> bookProcessor() {
        return book -> {
            MongoBook mongoBook = new MongoBook();
            mongoBook.setId(String.valueOf(book.getId()));
            mongoBook.setTitle(book.getTitle());

            MongoAuthor mongoAuthor = new MongoAuthor(book.getAuthor().getFullName());
            mongoAuthor.setId(String.valueOf(book.getAuthor().getId()));
            mongoBook.setAuthor(mongoAuthor);

            mongoBook.setGenres(new ArrayList<>());
            if (book.getGenres() != null) {
                book.getGenres().forEach(genre ->
                        mongoBook.getGenres().add(genreCache.get(genre.getId())));
            }
            return mongoBook;
        };
    }

    @Bean
    public ItemProcessor<Comment, MongoComment> commentProcessor() {
        return comment -> {
            MongoComment mongoComment = new MongoComment();
            mongoComment.setText(comment.getText());
            mongoComment.setBookId(String.valueOf(comment.getBook().getId()));
            return mongoComment;
        };
    }

    /* ---------- Шаги ---------- */
    @Bean(name = "authorsStep")
    public Step migrateAuthorsStep(JdbcCursorItemReader<Author> authorReader,
                                   ItemProcessor<Author, MongoAuthor> authorProcessor,
                                   MongoItemWriter<MongoAuthor> authorWriter) {
        return new StepBuilder("migrateAuthorsStep", jobRepository)
                .<Author, MongoAuthor>chunk(10, transactionManager)
                .reader(authorReader)
                .processor(authorProcessor)
                .writer(authorWriter)
                .listener(new ChunkListener() {
                    @Override
                    public void beforeChunk(@NonNull ChunkContext chunkContext) {
                        log.info("Начало пачки Авторов");
                    }

                    @Override
                    public void afterChunk(@NonNull ChunkContext chunkContext) {
                        log.info("Конец пачки Авторов");
                    }

                    @Override
                    public void afterChunkError(@NonNull ChunkContext chunkContext) {
                        log.info("Ошибка пачки Авторов");
                    }
                })
                .build();
    }

    @Bean(name = "genresStep")
    public Step migrateGenresStep(JdbcCursorItemReader<Genre> genreReader,
                                  ItemProcessor<Genre, MongoGenre> genreProcessor,
                                  MongoItemWriter<MongoGenre> genreWriter) {
        return new StepBuilder("migrateGenresStep", jobRepository)
                .<Genre, MongoGenre>chunk(10, transactionManager)
                .reader(genreReader)
                .processor(genreProcessor)
                .writer(genreWriter)
                .listener(new ChunkListener() {
                    @Override
                    public void beforeChunk(@NonNull ChunkContext chunkContext) {
                        log.info("Начало пачки Жанров");
                    }

                    @Override
                    public void afterChunk(@NonNull ChunkContext chunkContext) {
                        log.info("Конец пачки Жанров");
                    }

                    @Override
                    public void afterChunkError(@NonNull ChunkContext chunkContext) {
                        log.info("Ошибка пачки Жанров");
                    }
                })
                .build();
    }


    @Bean(name = "booksStep")
    public Step migrateBooksStep(JdbcCursorItemReader<Book> bookReader,
                                 ItemProcessor<Book, MongoBook> bookProcessor,
                                 MongoItemWriter<MongoBook> bookWriter) {
        return new StepBuilder("migrateBooksStep", jobRepository)
                .<Book, MongoBook>chunk(10, transactionManager)
                .reader(bookReader)
                .processor(bookProcessor)
                .writer(bookWriter)
                .listener(new ChunkListener() {
                    @Override
                    public void beforeChunk(@NonNull ChunkContext chunkContext) {
                        log.info("Начало пачки Книг");
                    }

                    @Override
                    public void afterChunk(@NonNull ChunkContext chunkContext) {
                        log.info("Конец пачки Книг");
                    }

                    @Override
                    public void afterChunkError(@NonNull ChunkContext chunkContext) {
                        log.info("Ошибка пачки Книг");
                    }
                })
                .build();
    }

    @Bean(name = "commentsStep")
    public Step migrateCommentsStep(JdbcCursorItemReader<Comment> commentReader,
                                    ItemProcessor<Comment, MongoComment> commentProcessor,
                                    MongoItemWriter<MongoComment> commentWriter) {
        return new StepBuilder("migrateCommentsStep", jobRepository)
                .<Comment, MongoComment>chunk(10, transactionManager)
                .reader(commentReader)
                .processor(commentProcessor)
                .writer(commentWriter)
                .listener(new ChunkListener() {
                    @Override
                    public void beforeChunk(@NonNull ChunkContext chunkContext) {
                        log.info("Начало пачки Комментариев");
                    }

                    @Override
                    public void afterChunk(@NonNull ChunkContext chunkContext) {
                        log.info("Конец пачки Комментариев");
                    }

                    @Override
                    public void afterChunkError(@NonNull ChunkContext chunkContext) {
                        log.info("Ошибка пачки Комментариев");
                    }
                })
                .build();
    }

    @Bean
    public Step cleanUpStep() {
        return new StepBuilder("cleanUpStep", jobRepository)
                .tasklet(cleanUpTasklet(), transactionManager)
                .build();
    }

    @Bean
    public Tasklet cleanUpTasklet() {
        return (contribution, chunkContext) -> {
            genreCache.clear();
            return RepeatStatus.FINISHED;
        };
    }

    @Bean(name = "parallelFlow")
    public Flow parallelFlow(
            @Qualifier("authorsStep") Step migrateAuthorsStep,
            @Qualifier("genresStep") Step migrateGenresStep
    ) {
        Flow authorsFlow = new FlowBuilder<Flow>("authorsFlow")
                .start(migrateAuthorsStep)
                .build();

        Flow genresFlow = new FlowBuilder<Flow>("genresFlow")
                .start(migrateGenresStep)
                .build();

        return new FlowBuilder<SimpleFlow>("parallelFlow")
                .split(new SimpleAsyncTaskExecutor())
                .add(authorsFlow, genresFlow)
                .build();
    }

    @Bean
    public Job migrationJob(@Qualifier("parallelFlow") Flow parallelFlow,
                            @Qualifier("booksStep") Step migrateBooksStep,
                            @Qualifier("commentsStep") Step migrateCommentsStep,
                            @Qualifier("cleanUpStep") Step cleanUpStep) {
        return new JobBuilder("migrationJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(parallelFlow)
                .next(migrateBooksStep)
                .next(migrateCommentsStep)
                .next(cleanUpStep)
                .build().build();
    }


}
