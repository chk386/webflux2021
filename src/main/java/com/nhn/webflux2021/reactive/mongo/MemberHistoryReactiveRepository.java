package com.nhn.webflux2021.reactive.mongo;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public interface MemberHistoryReactiveRepository extends ReactiveMongoRepository<MemberHistory, Integer> {}
