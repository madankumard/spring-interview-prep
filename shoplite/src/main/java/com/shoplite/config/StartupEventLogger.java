package com.shoplite.config;

import org.springframework.boot.availability.AvailabilityChangeEvent;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

public class StartupEventLogger implements ApplicationListener<ApplicationEvent> {

    @Override
    public void onApplicationEvent(ApplicationEvent event) {
        String name = event.getClass().getSimpleName();
        if(event instanceof AvailabilityChangeEvent<?> availability){
            name += " -> " + availability.getState();
        }
        System.out.println("### EVENT: "+ name);
    }
}
