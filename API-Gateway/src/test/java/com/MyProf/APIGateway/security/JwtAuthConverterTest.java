package com.MyProf.APIGateway.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Instant;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class JwtAuthConverterTest {

    private JwtAuthConverter converter;
    private Jwt jwt;

    @BeforeEach
    void setUp() {
        converter = new JwtAuthConverter();
        
        Map<String, Object> headers = new HashMap<>();
        headers.put("alg", "RS256");
        
        Map<String, Object> claims = new HashMap<>();
        claims.put("sub", "user123");
        claims.put("roles", Arrays.asList("USER", "ADMIN"));
        
        jwt = new Jwt(
            "token",
            Instant.now(),
            Instant.now().plusSeconds(300),
            headers,
            claims
        );
    }

    @Test
    void whenValidJwtWithRoles_thenConvertToAuthenticationToken() {
        Mono<AbstractAuthenticationToken> result = converter.convert(jwt);

        StepVerifier.create(result)
            .assertNext(token -> {
                assertThat(token).isInstanceOf(JwtAuthenticationToken.class);
                assertThat(token.isAuthenticated()).isTrue();
                assertThat(token.getAuthorities())
                    .extracting("authority")
                    .containsExactlyInAnyOrder("ROLE_USER", "ROLE_ADMIN");
            })
            .verifyComplete();
    }

    @Test
    void whenJwtWithoutRoles_thenConvertToAuthenticationTokenWithNoAuthorities() {
        Map<String, Object> headers = new HashMap<>();
        headers.put("alg", "RS256");
        
        Map<String, Object> claims = new HashMap<>();
        claims.put("sub", "user123");
        
        Jwt jwtWithoutRoles = new Jwt(
            "token",
            Instant.now(),
            Instant.now().plusSeconds(300),
            headers,
            claims
        );

        Mono<AbstractAuthenticationToken> result = converter.convert(jwtWithoutRoles);

        StepVerifier.create(result)
            .assertNext(token -> {
                assertThat(token).isInstanceOf(JwtAuthenticationToken.class);
                assertThat(token.isAuthenticated()).isTrue();
                assertThat(token.getAuthorities()).isEmpty();
            })
            .verifyComplete();
    }
}
