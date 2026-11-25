package com.shoplite.playground;

import org.springframework.boot.CommandLineRunner;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.PropertySource;
import org.springframework.stereotype.Component;

@Component
public class ConfigPrecedenceDemo implements CommandLineRunner {

    private static final String KEY = "shoplite.store.name";
    private final ConfigurableEnvironment environment;

    public ConfigPrecedenceDemo(ConfigurableEnvironment environment) {
        this.environment = environment;
    }

    @Override
    public void run(String... args) throws Exception {
        System.out.println("### CONFIG: " + KEY + " = " +  environment.getProperty(KEY));
        for(PropertySource<?> source : environment.getPropertySources()) {
            if(!source.getName().equals("configurationProperties") && source.containsProperty(KEY)) {
                System.out.println("### CONFIG: found in -> " + source.getName());
            }
        }
    }
}
