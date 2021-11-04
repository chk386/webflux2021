package com.nhn.webflux2021.configuration;

import com.nhn.webflux2021.reactive.mongo.MemberHistoryReactiveRepository;
import org.springframework.data.mongodb.repository.config.EnableReactiveMongoRepositories;

@EnableReactiveMongoRepositories(basePackageClasses = {MemberHistoryReactiveRepository.class})
public class MongoConfig {}