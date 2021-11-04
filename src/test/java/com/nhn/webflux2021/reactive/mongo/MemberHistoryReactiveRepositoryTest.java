package com.nhn.webflux2021.reactive.mongo;

import com.nhn.webflux2021.configuration.MongoConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.mongo.embedded.EmbeddedMongoAutoConfiguration;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.test.context.ContextConfiguration;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;

@DataMongoTest(excludeAutoConfiguration = EmbeddedMongoAutoConfiguration.class)
@ContextConfiguration(classes = MongoConfig.class)
class MemberHistoryReactiveRepositoryTest {
    @Autowired
    MemberHistoryReactiveRepository memberRepository;

    @Test
    void createHistoryTest() {
        MemberHistory memberHistory = new MemberHistory();
        memberHistory.setMemberId(1);
        memberHistory.setCreatedAt(LocalDateTime.now());

        memberRepository.save(memberHistory)
                .doOnNext(v -> System.out.println(v.toString()))
                .as(StepVerifier::create)
                .expectNextCount(1)
                .verifyComplete();
    }
}