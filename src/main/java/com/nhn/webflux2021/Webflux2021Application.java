package com.nhn.webflux2021;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.redis.RedisRepositoriesAutoConfiguration;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
import org.springframework.context.annotation.Bean;
import reactor.core.publisher.Sinks;

@SpringBootApplication(exclude = {RedisRepositoriesAutoConfiguration.class, WebMvcAutoConfiguration.class})
public class Webflux2021Application {

    public static void main(String[] args) {
        SpringApplication.run(Webflux2021Application.class, args);
    }

    @Bean
    public Sinks.Many<String> multicast() {
        return Sinks.many()
                    .multicast()
                    .directBestEffort();
    }
}


