package com.shoplite;

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
		SpringApplication.run(ShopliteApplication.class, args);
	}

	@Bean
	CommandLineRunner demo(OrderService  orderService) {
		return args -> orderService.placeOrder("alice@soplite.com","keyboard");
	}
}
