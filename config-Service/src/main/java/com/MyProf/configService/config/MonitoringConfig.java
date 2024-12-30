package com.MyProf.configService.config;

import io.micrometer.core.aop.TimedAspect;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.config.MeterFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.server.ServerWebExchange;

import java.util.List;

@Configuration
@EnableAspectJAutoProxy
public class MonitoringConfig {

    @Bean
    public TimedAspect timedAspect(MeterRegistry registry) {
        return new TimedAspect(registry);
    }

    @Bean
    public MeterRegistry.Config meterRegistryConfig(MeterRegistry meterRegistry) {
        return meterRegistry.config()
            .commonTags("application", "config-service")
            .meterFilter(MeterFilter.deny(id -> {
                String uri = id.getTag("uri");
                return uri != null && (uri.startsWith("/actuator") || uri.startsWith("/metrics"));
            }));
    }

    private List<Tag> getTags(ServerWebExchange exchange) {
        ServerHttpRequest request = exchange.getRequest();
        String clientIp = request.getHeaders().getFirst("X-Forwarded-For");
        if (clientIp == null) {
            clientIp = request.getRemoteAddress().getAddress().getHostAddress();
        }
        
        return List.of(
            Tag.of("client_ip", clientIp),
            Tag.of("method", request.getMethod().name()),
            Tag.of("uri", request.getURI().getPath())
        );
    }
}
