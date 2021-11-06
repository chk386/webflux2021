package com.nhn.webflux2021;

import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.test.StepVerifier;

import java.time.Duration;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;


public class MonoTest {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Test
    @DisplayName("mono를 처음 만들어보고 map을 이용하여 문자열을 숫자로 변환한다.")
    void monoTest() {
        final String ONE = "1";

        Mono.just(ONE)
            .log()
            .subscribeOn(Schedulers.newSingle("mono"))
            .doOnSubscribe(s -> logger.info("doOnSubscribe"))
            .doOnNext(v -> logger.info("data type is {}", v.getClass()))
            .map(Integer::parseInt)
            .doOnNext(v -> logger.info("data type is {}", v.getClass()))
            .subscribe(v -> logger.info("숫자 변환완료 : {}", v));
        // error, complete는 어떻게?
    }

    @Test
    @DisplayName("데이터를 처리중 에러 발생시 테스트")
    void monoError() {
        var notNum = "A";

        Mono.just(notNum)
            .log()
            .map(Integer::parseInt)
            .doOnError(e -> logger.error(e.toString()))
            .subscribe(v -> {
            }, e -> assertThat("A는 숫자가 아니다.", notNum, not(instanceOf(Integer.class))));
    }

    @Test
    @DisplayName("데이터를 처리중 에러 발생시 테스트")
    void monoError2() {
        Mono.error(NumberFormatException::new)
            .log()
            .doOnError(e -> assertThat("A는 숫자가 아니다.", e, Matchers.instanceOf(NumberFormatException.class)))
            .subscribe();
    }

    @Test
    @DisplayName("멀티 라인 문자열을 한 라인씩 1초 지연 후 subscriber에게 푸시를 하여 테스트를 진행한다.")
    void monoDelay() {
        var flux =
                Mono.just("hello\nwebflux")
                    .flatMapMany(s -> Flux.fromArray(s.split("\n"))
                                          .delayElements(Duration.ofSeconds(1))
                    ).log();

        StepVerifier.create(flux)
                    .expectNext("hello")
                    .expectNext("webflux")
                    .verifyComplete();
    }

    @Test
    @DisplayName("mono first, zip, zipWith 테스트")
    void monoFirst() {
        var mono1 = Mono.just("1")
                        .delayElement(Duration.ofSeconds(3));
        var mono2 = Mono.just("2")
                        .delayElement(Duration.ofSeconds(1));
        var mono3 = Mono.just("3")
                        .delayElement(Duration.ofSeconds(2));

        var first = Mono.firstWithSignal(mono1, mono2, mono3)
                        .log();

        StepVerifier.create(first)
                    .expectNext("2")
                    .verifyComplete();

        final long start = System.currentTimeMillis();

        StepVerifier.create(Mono.zip(mono1, mono2, mono3).log())
                    .consumeNextWith(tuple3 -> {
                        assertEquals("1", tuple3.getT1());
                        assertEquals("2", tuple3.getT2());
                        assertEquals("3", tuple3.getT3());

                        long time = System.currentTimeMillis() - start;
                        MatcherAssert.assertThat("3개의 mono.zip의 실행시간 약 3000ms이다.",
                                                 time,
                                                 lessThan(4000L));
                    })
                    .verifyComplete();

        var zipWith = Mono.just("A")
                          .zipWith(Mono.just(1), (s, num) -> s + num).log();

        StepVerifier.create(zipWith)
                    .expectNext("A1")
                    .verifyComplete();
    }
}
