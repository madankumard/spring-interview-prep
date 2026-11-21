package com.shoplite.notification;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

@Service
public class SmsNotificationService implements NotificationService{
    private static final Logger log = LoggerFactory.getLogger(SmsNotificationService.class);

    @Override
    public void send(String to, String message) {
        log.info("SMS to {}, {}", to, message);
    }
}
