package com.shoplite.injection;

import com.shoplite.notification.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class FieldInjectedReceiptService {

    @Autowired
    private NotificationService notificationService;

    public void sendReceipt(String to, String orderId) {
        notificationService.send(to, "[field] Receipt for order " + orderId);
    }
}