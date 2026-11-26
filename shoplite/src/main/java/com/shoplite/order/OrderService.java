package com.shoplite.order;

import com.shoplite.notification.NotificationService;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
public class OrderService {
    private final NotificationService notificationService;
    private final Counter ordersPlaced;

    public OrderService(@Qualifier("emailNotificationService") NotificationService notificationService,
                        MeterRegistry meterRegistry) {
        this.notificationService = notificationService;
        this.ordersPlaced = meterRegistry.counter("shoplite.orders.placed");
    }

    public void placeOrder(String customerEmail, String product){
        notificationService.send(customerEmail,"Order placed for " + product);
        ordersPlaced.increment();
    }
}
