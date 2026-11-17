package com.shoplite.concurrency;

import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicInteger;

@Component
public class SafeViewCounter {
    private final AtomicInteger count = new AtomicInteger();

    public void increment() {
        count.incrementAndGet(); // atomic
    }

    public int get() {
        return count.get();
    }
}