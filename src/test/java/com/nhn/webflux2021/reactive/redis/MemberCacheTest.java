package com.nhn.webflux2021.reactive.redis;

import com.nhn.webflux2021.reactive.ReactiveBaseTest;
import com.nhn.webflux2021.reactive.mongo.MemberHistory;
import com.nhn.webflux2021.reactive.mongo.MemberHistoryReactiveRepository;
import com.nhn.webflux2021.reactive.r2dbc.Member;
import com.nhn.webflux2021.reactive.r2dbc.MemberReactiveRepository;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;

@SpringBootTest
public class MemberCacheTest extends ReactiveBaseTest {
    Logger log = LoggerFactory.getLogger(this.getClass());

    @Autowired
    MemberReactiveRepository memberRepository;

    @Autowired
    MemberHistoryReactiveRepository memberHistoryReactiveRepository;

    @Autowired
    ReactiveRedisTemplate<String, Member> reactiveRedisTemplate;

    @Autowired
    TransactionalOperator transaction;

    @Test
    void cacheTest() {
        Member member = new Member(null, "Charles", "999-999-999");

        log.debug("쓰레드 살펴보기");

        transaction.execute(callback -> memberRepository.save(member)
                                                        .doOnNext(m -> log.debug("DB저장 후 id 채번 : {}", m.id()))
                                                        .doOnError(throwable -> callback.setRollbackOnly())
                                                        .as(transaction::transactional))
                   .flatMap(m -> {
                       MemberHistory memberHistory = new MemberHistory(m.id(), LocalDateTime.now());

                       Mono<MemberHistory> save = memberHistoryReactiveRepository.save(memberHistory);
                       Mono<Long> add = reactiveRedisTemplate.opsForSet()
                                                             .add(m.id()
                                                                   .toString(), member);

                       return Mono.zip(save, add);
                   })
                   .doOnNext(tuple -> log.debug("mongodb : {}, redis : {}", tuple.getT1(), tuple.getT2()))
                   .as(StepVerifier::create)
                   .expectNextCount(1)
                   .verifyComplete();
    }
}
