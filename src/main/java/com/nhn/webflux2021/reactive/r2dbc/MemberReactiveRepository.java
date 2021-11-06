package com.nhn.webflux2021.reactive.r2dbc;

import org.springframework.data.r2dbc.repository.R2dbcRepository;

public interface MemberReactiveRepository extends R2dbcRepository<Member, Integer> {
}
