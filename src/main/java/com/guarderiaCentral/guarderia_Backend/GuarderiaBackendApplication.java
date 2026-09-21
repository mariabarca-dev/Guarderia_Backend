package com.guarderiaCentral.guarderia_Backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

@SpringBootApplication
public class GuarderiaBackendApplication extends SpringBootServletInitializer {

	public static void main(String[] args) {
		SpringApplication.run(GuarderiaBackendApplication.class, args);
	}

	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
		return builder.sources(GuarderiaBackendApplication.class);
	}
}