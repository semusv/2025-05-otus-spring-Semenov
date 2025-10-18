package ru.otus.hw.config;

import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.timelimiter.TimeLimiterConfig;
import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4JCircuitBreakerFactory;
import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4JConfigBuilder;
import org.springframework.cloud.client.circuitbreaker.Customizer;
import org.springframework.context.annotation.Bean;

import java.time.Duration;


public class Resilience4jConfig {

    @Bean
    public Customizer<Resilience4JCircuitBreakerFactory> aggressiveCustomizer() {
        return factory -> factory.configureDefault(id -> new Resilience4JConfigBuilder(id)
                .circuitBreakerConfig(CircuitBreakerConfig.custom()
                        .slidingWindowType(CircuitBreakerConfig.SlidingWindowType.COUNT_BASED)
                        .slidingWindowSize(3) // Всего 3 вызова в окне
                        .failureRateThreshold(20) // Сработает при 20% ошибок
                        .waitDurationInOpenState(Duration.ofSeconds(10))
                        .permittedNumberOfCallsInHalfOpenState(1)
                        .minimumNumberOfCalls(1)
                        .build())
                .timeLimiterConfig(TimeLimiterConfig.custom()
                        .timeoutDuration(Duration.ofSeconds(1)) // Очень короткий таймаут
                        .cancelRunningFuture(true)
                        .build())
                .build());
    }

    @Bean
    public CircuitBreakerRegistry circuitBreakerRegistry() {
        return CircuitBreakerRegistry.ofDefaults();
    }
}