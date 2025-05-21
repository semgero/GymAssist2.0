package com.ProyectoAula.GymAssist;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;   
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class GymAssistApplication {

	public static void main(String[] args) {
		SpringApplication.run(GymAssistApplication.class, args);
	}

}
