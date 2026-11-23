package com.shoplite.order;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;

@Service
public class InventoryService {

    private final Map<String, Integer> stock = new HashMap<>(Map.of("keyboard", 5, "mouse", 0));

    public boolean reserve(String product, int qty) {
        int available = stock.getOrDefault(product, 0);
        if (available < qty) {
            return false;
        }
        stock.put(product, available - qty);
        return true;
    }
}
