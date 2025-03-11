package com.suken27.humanfactorsjava;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;

@SpringBootApplication
@ServletComponentScan // This is needed for the SlackAppController to work
public class HumanFactorsJavaApplication {

	public static void main(String[] args) {
		SpringApplication.run(HumanFactorsJavaApplication.class, args);
	}

}
