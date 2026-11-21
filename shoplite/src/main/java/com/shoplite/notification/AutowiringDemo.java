package com.shoplite.notification;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class AutowiringDemo implements CommandLineRunner {

    private final NotificationService emailNotificationService;

    public AutowiringDemo(@Qualifier("emailNotificationService") NotificationService emailNotificationService) {
        this.emailNotificationService = emailNotificationService;
    }

    @Override
    public void run(String... args) {
        System.out.println(">>> Injected: " + emailNotificationService.getClass().getSimpleName());
        emailNotificationService.send("alice@shoplite.com", "Order #1 confirmed");
    }
}