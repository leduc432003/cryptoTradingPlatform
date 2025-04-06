package com.duc;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class ChatboxApplication {

	public static void main(String[] args) {
		SpringApplication.run(ChatboxApplication.class, args);
	}

}
