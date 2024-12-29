package com.MyProf.configService.config;


import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.timelimiter.TimeLimiterConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.core.registry.EntryAddedEvent;
import io.github.resilience4j.core.registry.EntryRemovedEvent;
import io.github.resilience4j.core.registry.EntryReplacedEvent;
import io.github.resilience4j.core.registry.RegistryEventConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class ResilienceConfig {
    private static final Logger logger = LoggerFactory.getLogger(ResilienceConfig.class);

    @Bean
    public CircuitBreakerConfig circuitBreakerConfig() {
        return CircuitBreakerConfig.custom()
            .slidingWindowType(CircuitBreakerConfig.SlidingWindowType.COUNT_BASED)
            .slidingWindowSize(10)
            .failureRateThreshold(50)
            .waitDurationInOpenState(Duration.ofSeconds(10))
            .permittedNumberOfCallsInHalfOpenState(3)
            .slowCallRateThreshold(50)
            .slowCallDurationThreshold(Duration.ofSeconds(2))
            .build();
    }

    @Bean
    public TimeLimiterConfig timeLimiterConfig() {
        return TimeLimiterConfig.custom()
            .timeoutDuration(Duration.ofSeconds(3))
            .cancelRunningFuture(true)
            .build();
    }

    @Bean
    public RegistryEventConsumer<CircuitBreaker> circuitBreakerEventConsumer() {
        return new RegistryEventConsumer<CircuitBreaker>() {
            @Override
            public void onEntryAddedEvent(EntryAddedEvent<CircuitBreaker> entryAddedEvent) {
                entryAddedEvent.getAddedEntry().getEventPublisher()
                    .onStateTransition(event -> {
                        logger.info("Circuit Breaker {} state changed from {} to {}",
                            event.getCircuitBreakerName(),
                            event.getStateTransition().getFromState(),
                            event.getStateTransition().getToState());
                    });
            }

            @Override
            public void onEntryRemovedEvent(EntryRemovedEvent<CircuitBreaker> entryRemoveEvent) {
                logger.info("Circuit Breaker {} removed",
                    entryRemoveEvent.getRemovedEntry().getName());
            }

            @Override
            public void onEntryReplacedEvent(EntryReplacedEvent<CircuitBreaker> entryReplacedEvent) {
                logger.info("Circuit Breaker {} replaced",
                    entryReplacedEvent.getNewEntry().getName());
            }
        };
    }

    @Bean
    public CircuitBreakerRegistry circuitBreakerRegistry(
            CircuitBreakerConfig circuitBreakerConfig,
            RegistryEventConsumer<CircuitBreaker> circuitBreakerEventConsumer) {
        
        return CircuitBreakerRegistry.of(circuitBreakerConfig, circuitBreakerEventConsumer);
    }
}
