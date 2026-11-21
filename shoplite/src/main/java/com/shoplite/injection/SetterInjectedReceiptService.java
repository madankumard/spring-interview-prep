package com.shoplite.injection;

import com.shoplite.notification.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
public class SetterInjectedReceiptService {

    private static final Logger log = LoggerFactory.getLogger(SetterInjectedReceiptService.class);

    private NotificationService notificationService;

    public SetterInjectedReceiptService() {
        log.info("Setter: constructor called, notificationService = {}", notificationService);
    }

    @Autowired
    public void setNotificationService(@Qualifier("emailNotificationService") NotificationService notificationService) {
        log.info("Setter: setNotificationService() called with {}", notificationService.getClass().getSimpleName());
        this.notificationService = notificationService;
    }

    public void sendReceipt(String to, String orderId) {
        notificationService.send(to, "[setter] Receipt for order " + orderId);
    }
}