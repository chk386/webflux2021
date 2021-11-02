package com.nhn.webflux2021;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import reactor.core.publisher.Flux;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Observable;

import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.in;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.is;

@ExtendWith(OutputCaptureExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ReactiveStreamTest {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final List<Integer> integers = List.of(1, 2, 3, 4, 5);

    @Test
    @Order(1)
    @DisplayName("Iterable 테스트")
    void iterableTest(CapturedOutput output) {
        for (Iterator<Integer> it = integers.iterator(); it.hasNext(); ) {
            Integer integer = it.next();

            logger.info("Iterable Pattern : {}", integer);
        }

        assertThat("1,2,3,4,5가 출력되어야 한다.", captureOutput(output), everyItem(is(in(integers))));
    }

    @Test
    @Order(2)
    @DisplayName("Observable 테스트")
    void observableTest(CapturedOutput output) {
        ExamObservable observable = new ExamObservable();
        observable.addObserver((o, arg) -> logger.info("Observable Pattern : {}", arg));

        observable.push(integers);

        assertThat("1,2,3,4,5가 출력되어야 한다.", captureOutput(output), everyItem(is(in(integers))));
    }

    @SuppressWarnings("deprecation")
    static class ExamObservable extends Observable {
        void push(List<Integer> integers) {
            integers.forEach(i -> {
                this.setChanged();
                this.notifyObservers(i);
            });
        }
    }

    @Test
    @Order(3)
    @DisplayName("Reactive Streams 테스트")
    @SuppressWarnings("all")
    void reactiveStreamsTest(CapturedOutput output) {
        Publisher<Integer> publisher = s -> integers.forEach(s::onNext);
        publisher.subscribe(new Subscriber<>() {
            private final Logger logger = LoggerFactory.getLogger(this.getClass());
            Subscription subscription;


            @Override
            public void onSubscribe(Subscription s) {
                logger.info("onSubscribe");
                this.subscription = s;
            }

            @Override
            public void onNext(Integer integer) {
                // publisher가 데이터를 push할때 실행
                logger.info("Reactive Streams : {}", integer);

//                 시스템 로드 평균이 90을 넘을경우
//                if (ManagementFactory.getPlatformMXBean(OperatingSystemMXBean.class)
//                                     .getSystemLoadAverage() > 90) {
//                    this.subscription.request(3);
//                } else {
//                    this.subscription.request(1);
//                }
            }

            @Override
            public void onError(Throwable t) {
                logger.error(t.getMessage());
            }

            @Override
            public void onComplete() {
                logger.debug("onComplete");
            }
        });

        assertThat("1,2,3,4,5가 출력되어야 한다.", captureOutput(output), everyItem(is(in(integers))));
    }

    @Test
    @Order(4)
    @DisplayName("Reactor 테스트")
    void reactorTest(CapturedOutput output) {
        Flux.fromIterable(integers)
            .subscribe(v -> logger.info("Reactor : {}", v));

        assertThat("1,2,3,4,5가 출력되어야 한다.", captureOutput(output), everyItem(is(in(integers))));
    }

    private List<Integer> captureOutput(CapturedOutput output) {
        return Arrays.stream(output.getOut()
                                   .split("\n"))
                     .filter(line -> line.contains("DualityTest"))
                     .map(line -> Integer.parseInt(line.substring(line.length() - 1)))
                     .collect(toList());
    }
}