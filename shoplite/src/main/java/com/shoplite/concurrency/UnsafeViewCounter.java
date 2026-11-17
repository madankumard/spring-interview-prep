package com.shoplite.concurrency;

import org.springframework.stereotype.Component;

@Component
public class UnsafeViewCounter {
    private int count; // shared mutable state

    public void increment() {
        count++; // read, add, write: not atomic
    }

    public int get() {
        return count;
    }
}