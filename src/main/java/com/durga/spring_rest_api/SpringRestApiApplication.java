package com.durga.spring_rest_api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point for the Spring Boot product REST API.
 */
@SpringBootApplication
public class SpringRestApiApplication {

	/**
	 * Starts the application and its embedded web server.
	 *
	 * @param args command-line arguments passed to Spring Boot
	 */
	public static void main(String[] args) {
		SpringApplication.run(SpringRestApiApplication.class, args);
	}

}
