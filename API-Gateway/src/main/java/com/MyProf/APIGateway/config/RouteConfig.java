package com.MyProf.APIGateway.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RouteConfig {

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                // Auth Service Routes
                .route("auth-service", r -> r.path("/api/auth/**")
                        .filters(f -> f.stripPrefix(2)
                                .circuitBreaker(config -> config
                                        .setName("auth-service")
                                        .setFallbackUri("forward:/fallback/auth")))
                        .uri("lb://AUTH-SERVICE"))
                
                // Other service routes can be added here
                
                .build();
    }
}
