package com.bigboss.offbeatHub;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication(exclude = SecurityAutoConfiguration.class)
@EnableConfigurationProperties
public class OffbeatHubApplication {

	public static void main(String[] args) {
		SpringApplication.run(OffbeatHubApplication.class, args);
	}

}
