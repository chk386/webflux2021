package com.nhn.webflux2021;

import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class Webflux2021Application {

    public static void main(String[] args) {
        SpringApplication.run(Webflux2021Application.class, args);
    }

    // functional endpoint


    @Bean
    ApplicationRunner run(ApplicationContext context) {
        return args -> {

        };
    }


}
