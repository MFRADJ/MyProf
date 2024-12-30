package com.MyProf.APIGateway.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.reactive.server.WebTestClient;

@WebFluxTest(FallbackController.class)
class FallbackControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @Test
    void whenAuthFallback_thenReturnServiceUnavailable() {
        webTestClient.get()
            .uri("/fallback/auth")
            .exchange()
            .expectStatus().isEqualTo(HttpStatus.SERVICE_UNAVAILABLE)
            .expectBody()
            .jsonPath("$.message").isEqualTo("Auth Service is currently unavailable. Please try again later.")
            .jsonPath("$.status").isEqualTo("error");
    }
}
