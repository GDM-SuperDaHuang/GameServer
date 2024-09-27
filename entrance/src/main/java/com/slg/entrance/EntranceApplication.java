package com.slg.entrance;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
@SpringBootApplication
@ComponentScan(basePackages = {"com.slg.commom","com.slg.protobuffile", "com.slg.entrance"})
public class EntranceApplication {
	public static void main(String[] args) {
		System.out.println("开启...");
		SpringApplication.run(EntranceApplication.class, args);
	}

}
