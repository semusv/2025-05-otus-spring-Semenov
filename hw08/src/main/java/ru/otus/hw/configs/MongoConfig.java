package ru.otus.hw.configs;

import com.mongodb.event.CommandListener;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.mongo.MongoClientSettingsBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;


@Configuration
@RequiredArgsConstructor
public class MongoConfig {

    @Bean
    @Primary
    public MongoClientSettingsBuilderCustomizer mongoClientSettingsCustomizer(CommandListener commandListener) {
        return builder -> builder.addCommandListener(commandListener);
    }

}
