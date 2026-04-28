package com.example.casestudy.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE) // Changed to TYPE for class-level annotation
@Retention(RetentionPolicy.RUNTIME)
public @interface IpRateLimit {
    int limitForPeriod() default 50; // Higher default for controller-level
    long limitRefreshPeriodSeconds() default 60;
}