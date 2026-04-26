package com.example.casestudy.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface IpRateLimit {
    int limitForPeriod() default 5;
    long limitRefreshPeriodSeconds() default 60;
}