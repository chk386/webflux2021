package com.nhn.webflux2021.reactive.mongo;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;

public interface MemberHistoryReactiveRepository extends ReactiveCrudRepository<MemberHistory, Integer> {}
