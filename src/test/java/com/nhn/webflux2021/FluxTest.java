package com.nhn.webflux2021;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class FluxTest {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Test
    @DisplayName("flux생성 : just")
    void createFlux1() {
        var flux1 = Flux.just("1", "2", "3")
                        .log();

        StepVerifier.create(flux1)
                    .expectNext("1")
                    .expectNextCount(2)
                    .verifyComplete();
    }

    @Test
    @DisplayName("flux생성 : range")
    void createFlux2() {
        var monoFromFlux2 = Flux.range(0, 50)
                                .delayElements(Duration.ofMillis(100))
                                .publishOn(Schedulers.newSingle("single"))
                                .map(String::valueOf)
                                .doOnNext(logger::info)
                                .doOnComplete(() -> logger.info("데이터 처리가 완료 되었습니다."))
                                .collectList();

        StepVerifier.create(monoFromFlux2)
                    .expectNextCount(1)
                    .verifyComplete();
    }

    @Test
    @DisplayName("flux생성 : interval")
    void createFlux3() {
        var fluxBuffferList = Flux.interval(Duration.ofMillis(100))
                                  .log()
                                  .buffer(10);

        StepVerifier.create(fluxBuffferList.log()
                                           .take(3))
                    .recordWith(ArrayList::new)
                    .expectNextCount(1)
                    .expectNextCount(1)
                    .expectNextCount(1)
                    .consumeRecordedWith(v -> {
                        assertEquals(3, v.size());

                        assertEquals((int) v.stream()
                                            .mapToLong(Collection::size)
                                            .sum(),
                                     30);

                    })
                    .verifyComplete();
    }

    @Test
    @DisplayName("쓰레드 격리")
    void createFlux4() {
        var flux = Flux.just("A", "B", "C")
                       .log()
                       .publishOn(Schedulers.newSingle("SUBSCRIBER"))
                       .log()
                       .subscribeOn(Schedulers.newSingle("PUBLISHER"));

        StepVerifier.create(flux)
                    .expectNext("A")
                    .expectNext("B")
                    .expectNext("C")
                    .verifyComplete();
    }

    @Test
    @DisplayName("유용한 flux 유틸 테스트")
    void createFlux5() {
        Flux<String> flux1 = Flux.just(1, 2, 3, 4, 4, 4, 5, 6, 7, 8, 9, 9, 10)
                                 .delayElements(Duration.ofMillis(200))
                                 .publishOn(Schedulers.newSingle("AAAA"))
                                 .groupBy(v -> v)
                                 .log()
                                 .map(v -> "flux1 [data:" + v.key() + ", count:" + v.count() + "]");

        Flux<String> flux2 = Flux.range(100, 10)
                                 .delayElements(Duration.ofMillis(500))
                                 .publishOn(Schedulers.newSingle("BBBB"))
                                 .log()
                                 .map(v -> "flux2 : " + v);

        // 병합
        Flux<String> merged = Flux.merge(flux1, flux2);

        // 연결
        Flux<String> concat = Flux.concat(flux1, flux2);

        StepVerifier.create(concat)
                    .recordWith(ArrayList::new)
                    .expectNextCount(20)
                    .consumeRecordedWith(v -> {
                        for (String s : v) {
                            logger.info("[record] {}", s);
                        }
                    })
                    .verifyComplete();
    }
}
