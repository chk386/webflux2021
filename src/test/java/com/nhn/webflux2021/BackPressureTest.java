package com.nhn.webflux2021;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.BaseSubscriber;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.concurrent.CountDownLatch;

public class BackPressureTest {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Test
    void backpressureTest() {
        final var freeMemory = new CountDownLatch(100);
        BoardRepository boardRepository = new BoardRepository();
        Flux<Long> boards = boardRepository.findAll();

        boards.publishOn(Schedulers.newSingle("SUBSCRIBER"))
              .log()
              .subscribe(new BaseSubscriber<>() {
                  @Override
                  protected void hookOnNext(Long value) {
                      freeMemory.countDown();
                      var freeMem = freeMemory.getCount();

                      if (freeMem == 1) {
                          logger.warn("메모리 부족 -> pusblisher에게 cancel");
                          cancel();
                      }

                      if (freeMem <= 30 && freeMem > 1) {
                          logger.info("남은 메모리 용량 : {}%, 1개씩 전송해주세요", freeMem);
                          request(1);
                      } else if (freeMem % 10 == 0) {
                          request(10);
                      }
                  }

                  @Override
                  protected void hookOnCancel() {
                      logger.warn("publisher -> subscriber.onCanceled");
                  }
              });

        StepVerifier.create(boards)
                    .expectNextCount(99)
                    .thenCancel()
                    .verify();
    }

    static class BoardRepository {

        Flux<Long> findAll() {
            return Flux.interval(Duration.ofMillis(100));
        }
    }
}
