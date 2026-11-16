package com.shoplite.order;

import com.shoplite.notification.NotificationService;
import org.springframework.stereotype.Service;

@Service
public class OrderService {
    private final NotificationService notificationService;

    public OrderService(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    public void placeOrder(String customerEmail, String product){
        notificationService.send(customerEmail,"Order placed for " + product);
    }
}
