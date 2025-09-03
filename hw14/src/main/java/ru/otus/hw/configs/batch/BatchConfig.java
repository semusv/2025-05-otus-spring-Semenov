package ru.otus.hw.configs.batch;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Configuration
@Slf4j
@AllArgsConstructor
public class BatchConfig {

    private final JobRepository jobRepository;

    private final PlatformTransactionManager transactionManager;


    private final Logger logger = LoggerFactory.getLogger("Batch");

    /* ---------- Кэши ---------- */
    private final Map<Long, MongoAuthor> authorCache = new ConcurrentHashMap<>();

    private final Map<Long, MongoGenre> genreCache = new ConcurrentHashMap<>();

    private final Map<Long, String> bookIdCache = new ConcurrentHashMap<>();

    private final Map<Long, List<Long>> bookGenresCache = new ConcurrentHashMap<>();


    /* ---------- Processors ---------- */
    @Bean
    public ItemProcessor<Author, MongoAuthor> authorProcessor() {
        return author -> {
            MongoAuthor mo = new MongoAuthor(author.getFullName());
            mo.setId(new ObjectId().toString());
            authorCache.put(author.getId(), mo);
            return mo;
        };
    }

    @Bean
    public ItemProcessor<Genre, MongoGenre> genreProcessor() {
        return genre -> {
            MongoGenre mg = new MongoGenre(genre.getName());
            mg.setId(new ObjectId().toString());
            genreCache.put(genre.getId(), mg);
            return mg;
        };
    }

    @Bean
    public ItemProcessor<Map<String, Object>, Void> bookGenresProcessor() {
        return item -> {
            Long bookId = (Long) item.get("book_id");
            Long genreId = (Long) item.get("genre_id");
            bookGenresCache.computeIfAbsent(bookId, k -> new CopyOnWriteArrayList<>()).add(genreId);
            return null;
        };
    }

    @Bean
    public ItemProcessor<Book, MongoBook> bookProcessor() {
        return book -> {
            MongoBook mongoBook = new MongoBook();
            mongoBook.setId(ObjectId.get().toString());
            mongoBook.setAuthor(authorCache.get(book.getAuthor().getId()));
            mongoBook.setGenres(new ArrayList<>());
            bookGenresCache.get(book.getId()).forEach(
                    genreId -> mongoBook.getGenres().add(genreCache.get(genreId)));
            mongoBook.setTitle(book.getTitle());

            bookIdCache.put(book.getId(), mongoBook.getId());
            return mongoBook;
        };
    }

    @Bean
    public ItemProcessor<Comment, MongoComment> commentProcessor() {
        return comment -> new MongoComment(
                comment.getText(),
                bookIdCache.get(comment.getBook().getId())
        );
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
                        logger.info("Начало пачки Авторов");
                    }

                    @Override
                    public void afterChunk(@NonNull ChunkContext chunkContext) {
                        logger.info("Конец пачки Авторов");
                    }

                    @Override
                    public void afterChunkError(@NonNull ChunkContext chunkContext) {
                        logger.info("Ошибка пачки Авторов");
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
                        logger.info("Начало пачки Жанров");
                    }

                    @Override
                    public void afterChunk(@NonNull ChunkContext chunkContext) {
                        logger.info("Конец пачки Жанров");
                    }

                    @Override
                    public void afterChunkError(@NonNull ChunkContext chunkContext) {
                        logger.info("Ошибка пачки Жанров");
                    }
                })
                .build();
    }

    @Bean(name = "bookGenresStep")
    public Step migrateBookGenresStep(JdbcCursorItemReader<Map<String, Object>> reader,
                                      ItemProcessor<Map<String, Object>, Void> processor) {
        return new StepBuilder("migrateBookGenresStep", jobRepository)
                .<Map<String, Object>, Void>chunk(10, transactionManager)
                .reader(reader)
                .processor(processor)
                .writer(items -> {
                })
                .listener(new ChunkListener() {
                    @Override
                    public void beforeChunk(@NonNull ChunkContext chunkContext) {
                        logger.info("Начало пачки Связки книг-жанров");
                    }

                    @Override
                    public void afterChunk(@NonNull ChunkContext chunkContext) {
                        logger.info("Конец пачки Связки книг-жанров");
                    }

                    @Override
                    public void afterChunkError(@NonNull ChunkContext chunkContext) {
                        logger.info("Ошибка пачки Связки книг-жанров");
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
                        logger.info("Начало пачки Книг");
                    }

                    @Override
                    public void afterChunk(@NonNull ChunkContext chunkContext) {
                        logger.info("Конец пачки Книг");
                    }

                    @Override
                    public void afterChunkError(@NonNull ChunkContext chunkContext) {
                        logger.info("Ошибка пачки Книг");
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
                        logger.info("Начало пачки Комментариев");
                    }

                    @Override
                    public void afterChunk(@NonNull ChunkContext chunkContext) {
                        logger.info("Конец пачки Комментариев");
                    }

                    @Override
                    public void afterChunkError(@NonNull ChunkContext chunkContext) {
                        logger.info("Ошибка пачки Комментариев");
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
            authorCache.clear();
            genreCache.clear();
            bookGenresCache.clear();
            bookIdCache.clear();
            return RepeatStatus.FINISHED;
        };
    }

    @Bean(name = "parallelFlow")
    public Flow parallelFlow(
            @Qualifier("authorsStep") Step migrateAuthorsStep,
            @Qualifier("genresStep") Step migrateGenresStep,
            @Qualifier("bookGenresStep") Step migrateBookGenresStep
    ) {
        Flow authorsFlow = new FlowBuilder<Flow>("authorsFlow")
                .start(migrateAuthorsStep)
                .build();

        Flow genresFlow = new FlowBuilder<Flow>("genresFlow")
                .start(migrateGenresStep)
                .build();

        Flow bookGenresFlow = new FlowBuilder<Flow>("bookGenresFlow")
                .start(migrateBookGenresStep)
                .build();

        return new FlowBuilder<SimpleFlow>("parallelFlow")
                .split(new SimpleAsyncTaskExecutor())
                .add(authorsFlow, genresFlow, bookGenresFlow)
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
