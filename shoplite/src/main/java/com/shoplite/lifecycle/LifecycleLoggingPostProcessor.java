package com.shoplite.lifecycle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;

@Component
public class LifecycleLoggingPostProcessor implements BeanPostProcessor {

    private static final Logger log = LoggerFactory.getLogger(LifecycleLoggingPostProcessor.class);

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) {
        if (bean instanceof LifecycleDemoBean) {
            log.info("5. BeanPostProcessor.postProcessBeforeInitialization");
        }
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) {
        if (bean instanceof LifecycleDemoBean) {
            log.info("8. BeanPostProcessor.postProcessAfterInitialization");
        }
        return bean;
    }
}