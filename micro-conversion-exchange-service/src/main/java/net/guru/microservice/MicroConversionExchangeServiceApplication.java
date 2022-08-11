package net.guru.microservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class MicroConversionExchangeServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(MicroConversionExchangeServiceApplication.class, args);
	}

}
