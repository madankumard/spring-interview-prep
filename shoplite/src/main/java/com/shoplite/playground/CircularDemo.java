package com.shoplite.playground;

import com.shoplite.order.OrderPlacementService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class CircularDemo implements CommandLineRunner {

    private final OrderPlacementService orders;

    public CircularDemo(OrderPlacementService orders) {
        this.orders = orders;
    }

    @Override
    public void run(String... args) {
        System.out.println(">>> " + orders.placeOrder("ORD-1", "keyboard", 2));
        System.out.println(">>> " + orders.placeOrder("ORD-2", "mouse", 1));
    }
}
