package com.aims.prod;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.Async;

@SpringBootApplication
@Async
public class AutoInsuranceManagementSystem2Application {

	public static void main(String[] args) {
		SpringApplication.run(AutoInsuranceManagementSystem2Application.class, args);
	}

}
