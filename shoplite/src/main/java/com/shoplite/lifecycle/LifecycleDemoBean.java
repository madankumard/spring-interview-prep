package com.shoplite.lifecycle;

import com.shoplite.order.OrderService;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
public class LifecycleDemoBean implements BeanNameAware, ApplicationContextAware,
        InitializingBean, DisposableBean {

    private static final Logger log = LoggerFactory.getLogger(LifecycleDemoBean.class);

    public LifecycleDemoBean(OrderService orderService) {
        log.info("1. Constructor (OrderService injected: {})", orderService != null);
    }

    @Autowired
    public void injectEnvironment(Environment environment) {
        log.info("2. Setter injection (Environment injected: {})", environment != null);
    }

    @Override
    public void setBeanName(String name) {
        log.info("3. BeanNameAware.setBeanName: {}", name);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        log.info("4. ApplicationContextAware.setApplicationContext");
    }

    @PostConstruct
    void postConstruct() {
        log.info("6. @PostConstruct");
    }

    @Override
    public void afterPropertiesSet() {
        log.info("7. InitializingBean.afterPropertiesSet");
    }

    @PreDestroy
    void preDestroy() {
        log.info("9. @PreDestroy");
    }

    @Override
    public void destroy() {
        log.info("10. DisposableBean.destroy");
    }
}