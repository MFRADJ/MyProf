package com.MyProf.APIGateway.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.test.context.ActiveProfiles;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class RouteConfigTest {

    @Autowired
    private RouteLocator routeLocator;

    @Test
    void testAuthServiceRoute() {
        Flux<Route> routes = routeLocator.getRoutes();

        StepVerifier.create(routes)
            .assertNext(route -> {
                assertThat(route.getId()).isEqualTo("auth-service");
                assertThat(route.getUri().toString()).isEqualTo("lb://AUTH-SERVICE");
                
                // Verify predicates
                assertThat(route.getPredicate().toString())
                    .contains("/api/auth");
                
                // Verify filters
                assertThat(route.getFilters())
                    .anyMatch(filter -> filter.toString().contains("StripPrefix"));
            })
            .thenCancel()
            .verify();
    }

    @Test
    void testCircuitBreakerConfiguration() {
        Flux<Route> routes = routeLocator.getRoutes();

        StepVerifier.create(routes)
            .assertNext(route -> {
                assertThat(route.getFilters())
                    .anyMatch(filter -> filter.toString().contains("CircuitBreaker"));
            })
            .thenCancel()
            .verify();
    }
}
