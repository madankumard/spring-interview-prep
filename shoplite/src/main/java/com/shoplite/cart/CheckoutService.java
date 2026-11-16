package com.shoplite.cart;

import org.springframework.stereotype.Service;


@Service
public class CheckoutService {
    private final ShoppingCart cart; // prototype, but injected only once

    public CheckoutService(ShoppingCart cart) {
        this.cart = cart;
    }

    public String addItem(String customer, String item) {
        cart.add(item);
        return customer + " uses cart #" + cart.getId() + " with " + cart.getItems();
    }
}