package com.courseproject.gateway_server.filters;

import io.micrometer.tracing.Tracer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Mono;

import java.util.Objects;

@Configuration
public class ResponseFilter {
 
    final Logger logger =LoggerFactory.getLogger(ResponseFilter.class);

    @Autowired
    private Tracer tracer;
    
    @Autowired
	FilterUtils filterUtils;
 
    @Bean
    public GlobalFilter postGlobalFilter() {
        return (exchange, chain) -> {
            String traceId = tracer.currentSpan() != null ? Objects.requireNonNull(tracer.currentSpan()).context().traceId() :
                    exchange.getRequest().getHeaders().getFirst(FilterUtils.CORRELATION_ID);
            return chain.filter(exchange).then(Mono.fromRunnable(() -> {
            	  logger.debug("Adding the correlation id to the outbound headers. {}", traceId);
                  exchange.getResponse().getHeaders().add(FilterUtils.CORRELATION_ID, traceId);
                  logger.debug("Completing outgoing request for {}.", exchange.getRequest().getURI());
              }));
        };
    }
}
