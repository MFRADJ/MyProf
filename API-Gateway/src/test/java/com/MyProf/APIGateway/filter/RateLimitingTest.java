package com.MyProf.APIGateway.filter;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class RateLimitingTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void whenExceedingRateLimit_thenReturn429() throws Exception {
        // Create a thread pool for concurrent requests
        ExecutorService executor = Executors.newFixedThreadPool(5);
        List<CompletableFuture<ResponseEntity<String>>> futures = new ArrayList<>();

        // Make 10 concurrent requests
        for (int i = 0; i < 10; i++) {
            CompletableFuture<ResponseEntity<String>> future = CompletableFuture.supplyAsync(() ->
                restTemplate.getForEntity(
                    "http://localhost:" + port + "/api/auth/status",
                    String.class
                ),
                executor
            );
            futures.add(future);
        }

        // Wait for all requests to complete
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

        // Verify that some requests were rate limited
        long rateLimitedRequests = futures.stream()
            .map(CompletableFuture::join)
            .filter(response -> response.getStatusCode() == HttpStatus.TOO_MANY_REQUESTS)
            .count();

        assertThat(rateLimitedRequests).isGreaterThan(0);

        executor.shutdown();
    }
}
