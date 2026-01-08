package com.meetup.server.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

@Configuration
public class ThreadConfig {

    @Bean
    public ExecutorService routeExecutor() {
        ThreadFactory threadFactory = Thread.ofVirtual().name("route-thread-", 0).factory();
        return Executors.newThreadPerTaskExecutor(threadFactory);
    }
}
