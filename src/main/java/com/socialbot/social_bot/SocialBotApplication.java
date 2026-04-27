package com.socialbot.social_bot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class SocialBotApplication {

	public static void main(String[] args) {
		SpringApplication.run(SocialBotApplication.class, args);
	}

}
