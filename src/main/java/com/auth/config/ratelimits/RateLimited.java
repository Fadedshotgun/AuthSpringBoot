package com.auth.config.ratelimits;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public  @interface RateLimited {
    int capacity() default 10;     // Max allowed requests
    int tokensPerPeriod() default 10; // Max allowed requests per x amount of time
    int periodInSeconds() default 60; // Time window
}