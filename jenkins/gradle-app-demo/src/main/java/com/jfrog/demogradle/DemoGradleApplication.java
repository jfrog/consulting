package com.jfrog.demogradle;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.mongo.embedded.EmbeddedMongoAutoConfiguration;

@SpringBootApplication(exclude = {EmbeddedMongoAutoConfiguration.class})

public class DemoGradleApplication {

	public static void main(String[] args) {
		SpringApplication.run(DemoGradleApplication.class, args);
	}
}

