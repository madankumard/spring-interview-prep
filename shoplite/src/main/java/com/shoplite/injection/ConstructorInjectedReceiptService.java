package com.shoplite.injection;

import com.shoplite.notification.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
public class ConstructorInjectedReceiptService {

    private static final Logger log = LoggerFactory.getLogger(ConstructorInjectedReceiptService.class);

    private final NotificationService notificationService;

    public ConstructorInjectedReceiptService(@Qualifier("emailNotificationService") NotificationService notificationService) {
        log.info("Constructor: dependency available immediately: {}", notificationService.getClass().getSimpleName());
        this.notificationService = notificationService;
    }

    public void sendReceipt(String to, String orderId) {
        notificationService.send(to, "[constructor] Receipt for order " + orderId);
    }
}