package com.cuet.ghoorni;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class GhoorniApplication {

	public static void main(String[] args) {
		SpringApplication.run(GhoorniApplication.class, args);
	}

}
