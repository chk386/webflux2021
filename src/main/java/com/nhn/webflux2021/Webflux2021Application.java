package com.nhn.webflux2021;

import com.nhn.webflux2021.reactive.r2dbc.MemberReactiveRepository;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.redis.RedisRepositoriesAutoConfiguration;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories;

@SpringBootApplication(exclude = {RedisRepositoriesAutoConfiguration.class, WebMvcAutoConfiguration.class})
@EnableR2dbcRepositories(basePackageClasses = MemberReactiveRepository.class)
public class Webflux2021Application {

    public static void main(String[] args) {
        SpringApplication.run(Webflux2021Application.class, args);
    }

    @Bean
    ApplicationRunner run(ApplicationContext context) {

        System.out.println("aa");

        return args -> {

        };
    }
}


