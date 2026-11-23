package com.shoplite.cart;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;


@Service
public class CheckoutService {
    private final ObjectProvider<ShoppingCart> cartProvider;

    public CheckoutService(ObjectProvider<ShoppingCart> cartProvider) {
        this.cartProvider = cartProvider;
    }

    public String addItem(String customer, String item) {
        ShoppingCart cart = cartProvider.getObject(); //new prototype every call
        cart.add(item);
        return customer + " uses cart #" + cart.getId() + " with " + cart.getItems();
    }
}