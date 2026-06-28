package com.logicminers.banking.auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class AuthServiceApplication {

	public static void main(String[] args) {
		// This is the spark that ignites the entire microservice!
		SpringApplication.run(AuthServiceApplication.class, args);
	}
}