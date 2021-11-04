package com.nhn.webflux2021.reactive.mongo;

import com.nhn.webflux2021.configuration.MongoConfig;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.mongo.embedded.EmbeddedMongoAutoConfiguration;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.test.context.ContextConfiguration;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.time.LocalDateTime;

@DataMongoTest(excludeAutoConfiguration = EmbeddedMongoAutoConfiguration.class)
@ContextConfiguration(classes = MongoConfig.class)
class MemberHistoryReactiveRepositoryTest {

    Logger log = LoggerFactory.getLogger(this.getClass());

    @Autowired
    MemberHistoryReactiveRepository memberRepository;

    @Test
    void createHistoryTest() {
        Flux.range(1, 1000)
            .map(MemberHistoryReactiveRepositoryTest::of)
            .doOnNext(memberHistory -> System.out.println(memberHistory.toString()))
            .as(memberRepository::saveAll)
            .doOnNext(memberHistory -> log.debug("result : {}", memberHistory.toString()))
            .as(StepVerifier::create)
            .expectNextCount(1000)
            .verifyComplete();
    }

    private static MemberHistory of(Integer id) {
        MemberHistory memberHistory = new MemberHistory();
        memberHistory.setMemberId(id);
        memberHistory.setCreatedAt(LocalDateTime.now());

        return memberHistory;
    }
}