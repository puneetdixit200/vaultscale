package com.vaultscale;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication //creating beans
public class VaultScaleApplication {

    /**
     * This is the main method — the FIRST thing Java runs.
     * SpringApplication.run() boots the entire Spring context,
     * starts the embedded Tomcat server, connects to the database,
     * and makes the app ready to handle HTTP requests.
     */

	public static void main(String[] args) {
		SpringApplication.run(VaultScaleApplication.class, args);
		System.out.println("VaultScale Backend is running on localhost:8080");
	}

}
