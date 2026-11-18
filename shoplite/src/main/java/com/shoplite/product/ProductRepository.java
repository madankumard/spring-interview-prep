package com.shoplite.product;

import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class ProductRepository {

    private final Map<Long, Product> store = new ConcurrentHashMap<>(Map.of(
            1L, new Product(1L, "Keyboard", new BigDecimal("49.99")),
            2L, new Product(2L, "Mouse", new BigDecimal("19.99"))
    ));

    public List<Product> findAll() {
        return List.copyOf(store.values());
    }

    public Optional<Product> findById(Long id) {
        return Optional.ofNullable(store.get(id));
    }
}