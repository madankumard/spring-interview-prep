package com.shoplite.order;

import org.springframework.stereotype.Service;

@Service
public class OrderPlacementService {

    private final InventoryService inventory;

    public OrderPlacementService(InventoryService inventory) {
        this.inventory = inventory;
    }

    public String placeOrder(String orderId, String product, int qty) {
        if (!inventory.reserve(product, qty)) {
            cancel(orderId);
            return "Order " + orderId + " rejected: not enough " + product;
        }
        return "Order " + orderId + " placed for " + qty + " x " + product;
    }

    public void cancel(String orderId) {
        System.out.println(">>> Order " + orderId + " cancelled");
    }
}
