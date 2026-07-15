package com.auth.config.ratelimits;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.server.ResponseStatusException;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import jakarta.servlet.http.HttpServletRequest;

@Aspect
@Component
public class RateLimitingComponent {

    private final Map<String, Bucket> cache = new ConcurrentHashMap<>();

    @Before("@annotation(rateLimited)")
    public void beforeRequest(RateLimited rateLimited) {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        
        String ip = request.getRemoteAddr();
        String mappingKey = ip + "-" + request.getRequestURI();

        Bucket bucket = cache.computeIfAbsent(mappingKey, k -> createNewBucket(rateLimited));

        if (!bucket.tryConsume(1)) {
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, "Rate limited");
        }
    }

    private Bucket createNewBucket(RateLimited limit) {
        return Bucket.builder().addLimit(Bandwidth.builder()
        		.capacity(limit.capacity())
        		.refillGreedy(limit.tokensPerPeriod(), Duration.ofSeconds(limit.periodInSeconds()))
        		.build()).build();      
    }
    
    public void clearCache() {
        cache.clear();
    }
}