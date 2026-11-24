package com.shoplite;

import com.shoplite.config.StartupEventLogger;
import com.shoplite.order.OrderService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
@ConfigurationPropertiesScan
public class ShopliteApplication {

	public static void main(String[] args) {
		SpringApplication app =new SpringApplication(ShopliteApplication.class);
		app.addListeners(new StartupEventLogger());
		app.run(args);
	}

	@Bean
	CommandLineRunner demo(OrderService  orderService) {
		return args -> {
			System.out.println("### RUNNER: CommandLineRunner running");
			orderService.placeOrder("alice@soplite.com","keyboard");
		};
	}
}
