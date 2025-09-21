package ru.otus.hw.configs.batch;

import org.springframework.batch.item.data.MongoItemWriter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;
import ru.otus.hw.models.mongo.MongoAuthor;
import ru.otus.hw.models.mongo.MongoBook;
import ru.otus.hw.models.mongo.MongoComment;
import ru.otus.hw.models.mongo.MongoGenre;

@Configuration
public class BatchWriterConfig {

    @Bean
    public MongoItemWriter<MongoAuthor> authorWriter(MongoTemplate mongoTemplate) {
        MongoItemWriter<MongoAuthor> writer = new MongoItemWriter<>();
        writer.setTemplate(mongoTemplate);
        writer.setCollection("authors");
        return writer;
    }

    @Bean
    public MongoItemWriter<MongoBook> bookWriter(MongoTemplate mongoTemplate) {
        MongoItemWriter<MongoBook> writer = new MongoItemWriter<>();
        writer.setTemplate(mongoTemplate);
        writer.setCollection("books");
        return writer;
    }

    @Bean
    public MongoItemWriter<MongoGenre> genreWriter(MongoTemplate mongoTemplate) {
        MongoItemWriter<MongoGenre> writer = new MongoItemWriter<>();
        writer.setTemplate(mongoTemplate);
        writer.setCollection("genres");
        return writer;
    }

    @Bean
    public MongoItemWriter<MongoComment> commentWriter(MongoTemplate mongoTemplate) {
        MongoItemWriter<MongoComment> writer = new MongoItemWriter<>();
        writer.setTemplate(mongoTemplate);
        writer.setCollection("comments");
        return writer;
    }
}
