package com.shoplite.concurrency;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
class ThreadSafetyDemo implements CommandLineRunner {
    private static final Logger log = LoggerFactory.getLogger(ThreadSafetyDemo.class);

    private final UnsafeViewCounter unsafeCounter;
    private final SafeViewCounter safeCounter;

    ThreadSafetyDemo(UnsafeViewCounter unsafeCounter, SafeViewCounter safeCounter) {
        this.unsafeCounter = unsafeCounter;
        this.safeCounter = safeCounter;
    }

    @Override
    public void run(String... args) {
        int tasks = 1_000;
        int incrementsPerTask = 1_000;

        try (ExecutorService pool = Executors.newFixedThreadPool(8)) {
            for (int i = 0; i < tasks; i++) {
                pool.submit(() -> {
                    for (int j = 0; j < incrementsPerTask; j++) {
                        unsafeCounter.increment();
                        safeCounter.increment();
                    }
                });
            }
        } // leaving the try block waits for all tasks to finish

        log.info("Expected:       {}", tasks * incrementsPerTask);
        log.info("Unsafe counter: {}", unsafeCounter.get());
        log.info("Safe counter:   {}", safeCounter.get());
    }
}