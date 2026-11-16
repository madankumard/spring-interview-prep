package com.shoplite.playground;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class ContainerDemo {
    static class LoudBean { // no @Component, so the main app's scan ignores it
        LoudBean() {
            System.out.println("  LoudBean constructor");
        }

        @PostConstruct
        void init() {
            System.out.println("  LoudBean @PostConstruct");
        }
    }

    public static void main(String[] args) {
        System.out.println("--- Plain BeanFactory ---");
        DefaultListableBeanFactory factory = new DefaultListableBeanFactory();
        factory.registerBeanDefinition("loudBean", new RootBeanDefinition(LoudBean.class));
        System.out.println("Definition registered, nothing created yet");
        factory.getBean(LoudBean.class);
        System.out.println("After getBean()");

        System.out.println("--- ApplicationContext ---");
        try (var context = new AnnotationConfigApplicationContext(LoudBean.class)) {
            System.out.println("Context refreshed, bean already created");
            context.getBean(LoudBean.class);
            System.out.println("After getBean() (no new instance)");
        }
    }
}
