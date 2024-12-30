package com.MyProf.APIGateway.filter;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpMethod;
import org.springframework.http.server.PathContainer;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.http.server.RequestPath;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import org.springframework.core.Ordered;

import java.net.InetSocketAddress;

import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class LoggingFilterTest {

    @Mock
    private ServerWebExchange exchange;

    @Mock
    private GatewayFilterChain chain;

    @Mock
    private ServerHttpRequest request;

    @Mock
    private RequestPath requestPath;

    @InjectMocks
    private LoggingFilter loggingFilter;

    @Test
    void whenFilterRequest_thenLogAndContinueChain() {
        // Arrange
        when(exchange.getRequest()).thenReturn(request);
        when(request.getMethod()).thenReturn(HttpMethod.GET);
        when(request.getPath()).thenReturn(requestPath);
        when(requestPath.value()).thenReturn("/test");
        when(request.getRemoteAddress()).thenReturn(InetSocketAddress.createUnresolved("localhost", 8080));
        when(chain.filter(exchange)).thenReturn(Mono.empty());

        // Act
        Mono<Void> result = loggingFilter.filter(exchange, chain);

        // Assert
        StepVerifier.create(result)
            .verifyComplete();

        verify(chain).filter(exchange);
        verify(request).getMethod();
        verify(request).getPath();
        verify(request).getRemoteAddress();
    }

    @Test
    void testFilterOrder() {
        assertThat(loggingFilter.getOrder()).isEqualTo(Ordered.HIGHEST_PRECEDENCE);
    }
}
