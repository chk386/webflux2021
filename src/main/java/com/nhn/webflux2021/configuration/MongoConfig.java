package com.nhn.webflux2021.configuration;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.repository.config.EnableReactiveMongoRepositories;

@Configuration
@EnableReactiveMongoRepositories("com.nhn.webflux2021.reactive.mongo")
public class MongoConfig {
}