package com.shoplite.notification;

import java.util.List;
import java.util.Map;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class NotificationDispatcher implements CommandLineRunner {

    private final Map<String, NotificationService> channels;
    private final List<NotificationService> all;

    public NotificationDispatcher(Map<String, NotificationService> channels,
                                  List<NotificationService> all) {
        this.channels = channels;
        this.all = all;
    }

    @Override
    public void run(String... args) {
        System.out.println(">>> Map keys: " + channels.keySet());
        System.out.println(">>> List size: " + all.size());
    }
}