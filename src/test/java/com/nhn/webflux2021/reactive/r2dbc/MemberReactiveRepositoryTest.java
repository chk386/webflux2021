package com.nhn.webflux2021.reactive.r2dbc;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.test.StepVerifier;

import java.math.BigInteger;

@DataR2dbcTest
class MemberReactiveRepositoryTest {

    @Autowired
    MemberReactiveRepository memberRepository;

    @Autowired
    R2dbcEntityTemplate template;

    @Test
    void memberRepositoryTest() {
        StepVerifier.create(memberRepository.findById(1)
                                            .map(Member::id))
                    .expectNext(1)
                    .verifyComplete();
    }

    @Autowired
    TransactionalOperator transaction;

    @Test
    void addMemberTest() {
        Member member = new Member(null, "nhn-commerce", "");

        StepVerifier.create(memberRepository.save(member)
                                            .map(Member::id)
                                            .as(transaction::transactional))
                    .expectNextCount(1)
                    .verifyComplete();
    }

    @Test
    void templateTest() {
        template.getDatabaseClient()
                .sql("SELECT * FROM member WHERE id = :id")
                .bind("id", 1)
                .fetch()
                .one()
                .map(resultSet -> (BigInteger) resultSet.get("id"))
                .as(StepVerifier::create)
                .expectNext(BigInteger.ONE)
                .verifyComplete();
    }

    @Test
    void transactionTest() {
        final Member member = new Member(null, "nhn-commerce", "");

        transaction.transactional(memberRepository.save(member))
                   .as(StepVerifier::create)
                   .expectNextCount(1)
                   .verifyComplete();

        final Member member2 = new Member(null, "nhn-programmaticaaaaaaaaapprogrammaticaaaaaaaaapprogrammaticaaaaaaaaapprogrammaticaaaaaaaaapprogrammaticaaaaaaaaapprogrammaticaaaaaaaaapprogrammaticaaaaaaaaapprogrammaticaaaaaaaaapprogrammaticaaaaaaaaapprogrammaticaaaaaaaaapprogrammaticaaaaaaaaapprogrammaticaaaaaaaaapprogrammaticaaaaaaaaaprogrammaticaaaaaaaaaprogrammaticaaaaaaaaaprogrammaticaaaaaaaaaprogrammaticaaaaaaaaa", "");

        transaction.execute(callback -> memberRepository.save(member2)
                                                        .doOnError(throwable -> callback.setRollbackOnly()))
                   .as(StepVerifier::create)
                   .expectError()
                   .verify();
    }
}