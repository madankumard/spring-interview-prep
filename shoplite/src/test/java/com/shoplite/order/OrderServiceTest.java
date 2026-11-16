package com.shoplite.order;

import com.shoplite.notification.NotificationService;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class OrderServiceTest {

    @Test
    public void placeOrderSendsNotification() {
        List<String> sent = new ArrayList<>();
        NotificationService fake = (to, msg) -> sent.add(to + ":" + msg);
        new OrderService(fake).placeOrder("bob@shoplite.com","Mouse");
        assertThat(sent).containsExactly("bob@shoplite.com:Order placed for Mouse");
    }

}
