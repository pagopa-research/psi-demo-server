package it.lockless.psidemoserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Contains the main method that starts the server.
 * Note: This server is intended to be used for testing. In order to use it in a production environment,
 * this code needs to be further extended to ensure a proper use of resources and a stricter security configuration.
 */
@SpringBootApplication
@Configuration
@EnableScheduling
public class PsiDemoServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(PsiDemoServerApplication.class, args);
	}
}
