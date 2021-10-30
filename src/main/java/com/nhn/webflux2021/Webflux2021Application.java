package com.nhn.webflux2021;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import reactor.core.publisher.Flux;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Flow;
import java.util.concurrent.FutureTask;
import java.util.stream.Collectors;

@SpringBootApplication
public class Webflux2021Application {

    public static void main(String[] args) {
        SpringApplication.run(Webflux2021Application.class, args);

        Flux.
    }

    @Bean
    ApplicationRunner run(ApplicationContext context) {
        return args -> {

        };
    }
}
