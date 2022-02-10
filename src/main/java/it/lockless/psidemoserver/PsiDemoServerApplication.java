package it.lockless.psidemoserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * This class contains the method that start the whole server.
 * Note: This server is intended to be used for testing. In order to use it in a business environment,
 * the server needs to be further extended to ensure proper use of security configurations and resources.
 */
@SpringBootApplication
@Configuration
@EnableScheduling
public class PsiDemoServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(PsiDemoServerApplication.class, args);
	}
}
