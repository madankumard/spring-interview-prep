package com.shoplite.cart;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class ShoppingCart {
    private static final Logger log = LoggerFactory.getLogger(ShoppingCart.class);
    private static final AtomicInteger COUNTER = new AtomicInteger();

    private final int id = COUNTER.incrementAndGet();
    private final List<String> items = new ArrayList<>();

    public ShoppingCart() {
        log.info("ShoppingCart #{} created", id);
    }

    @PostConstruct
    void init() {
        log.info("ShoppingCart #{} @PostConstruct", id);
    }

    @PreDestroy
    void cleanup() {
        log.info("ShoppingCart #{} @PreDestroy (should never print)", id);
    }

    public void add(String item) {
        items.add(item);
    }

    public int getId() {
        return id;
    }

    public List<String> getItems() {
        return items;
    }
}
