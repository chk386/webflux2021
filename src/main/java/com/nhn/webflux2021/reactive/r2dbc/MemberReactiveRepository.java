package com.nhn.webflux2021.reactive.r2dbc;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface MemberReactiveRepository extends ReactiveCrudRepository<Member, Integer> {}
