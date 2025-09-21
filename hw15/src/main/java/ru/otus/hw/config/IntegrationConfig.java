package ru.otus.hw.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.MessageChannelSpec;
import org.springframework.integration.dsl.MessageChannels;
import org.springframework.integration.dsl.PollerSpec;
import org.springframework.integration.dsl.Pollers;
import org.springframework.integration.scheduling.PollerMetadata;
import ru.otus.hw.domain.Notification;
import ru.otus.hw.domain.ParsedLog;
import ru.otus.hw.service.LogAnalysisService;
import ru.otus.hw.service.LogFilterService;
import ru.otus.hw.service.LogParserService;
import ru.otus.hw.service.NotificationService;

import java.util.Objects;


@Configuration
public class IntegrationConfig {

    @Bean
    public MessageChannelSpec<?, ?> rawLogsChannel() {
        return MessageChannels.queue(200);
    }

    @Bean
    public MessageChannelSpec<?, ?> parsedLogsChannel() {
        return MessageChannels.direct();
    }

    @Bean
    public MessageChannelSpec<?, ?> filteredLogsChannel() {
        return MessageChannels.direct();
    }

    @Bean
    public MessageChannelSpec<?, ?> analysisChannel() {
        return MessageChannels.direct();
    }

    @Bean
    public MessageChannelSpec<?, ?> notificationsChannel() {
        return MessageChannels.publishSubscribe();
    }

    @Bean(name = PollerMetadata.DEFAULT_POLLER)
    public PollerSpec poller() {
        return Pollers.fixedRate(100).maxMessagesPerPoll(10);
    }

    @Bean
    public IntegrationFlow logProcessingFlow(
            LogParserService logParserService,
            LogFilterService logFilterService,
            LogAnalysisService logAnalysisService,
            NotificationService notificationService) {

        return IntegrationFlow.from(rawLogsChannel())
                .split()
                .handle(logParserService, "parse")
                .channel(parsedLogsChannel())
                .handle(logFilterService, "filter")
                .<ParsedLog>filter(Objects::nonNull,
                        filter -> filter.discardChannel("nullChannel"))
                .channel(filteredLogsChannel())
                .handle(logAnalysisService, "analyze")
                .channel(analysisChannel())
                .handle(notificationService, "generateNotification")
                .<Notification>filter(Objects::nonNull,
                        filter -> filter.discardChannel("nullChannel"))
                .aggregate(aggregator -> aggregator
                        .groupTimeout(1000L)
                        .sendPartialResultOnExpiry(true)
                        .expireGroupsUponCompletion(true)
                        .expireGroupsUponTimeout(true)
                )
                .channel(notificationsChannel())
                .get();
    }
}