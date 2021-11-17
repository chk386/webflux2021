package com.nhn.webflux2021;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import java.util.Arrays;
import java.util.List;

import static java.util.stream.Collectors.toList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;

@ExtendWith(OutputCaptureExtension.class)
public class HotPublisherTest {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Test
    @DisplayName("flux: cold 테스트")
    void coldTest(CapturedOutput output) throws InterruptedException {
        final var source = Flux.fromIterable(List.of("blue", "green", "orange", "purple"))
                               .map(String::toUpperCase);

        source.subscribe(color1 -> logger.info("subscriber1 --- {}", color1));
        Thread.sleep(3000);
        source.subscribe(color2 -> logger.info("subscriber2 --- {}", color2));

        assertThat("2번 실행된다.",
                   extractColorsFromConsole(output),
                   contains("BLUE", "GREEN", "ORANGE", "PURPLE", "BLUE", "GREEN", "ORANGE", "PURPLE"));
    }

    @Test
    @DisplayName("flux: hot 테스트")
    void hotTest(CapturedOutput output) {
        Sinks.Many<Object> hot = Sinks.many()
                                      .multicast()
                                      .directBestEffort();
        var hotFlux = hot.asFlux();

        hot.tryEmitNext("BLACK");
        hot.tryEmitNext("RED");
        hotFlux.subscribe(d -> logger.info("Subscriber 1 --- {}", d));

        hot.tryEmitNext("BLUE");
        hot.tryEmitNext("GREEN");

        hotFlux.subscribe(d -> logger.info("Subscriber 2 --- {}", d));

        hot.tryEmitNext("ORANGE");
        hot.tryEmitNext("PURPLE");
        hot.tryEmitComplete();


        assertThat("hot publisher 테스트",
                   extractColorsFromConsole(output),
                   contains("BLUE", "GREEN", "ORANGE", "ORANGE", "PURPLE", "PURPLE"));
    }

    private List<String> extractColorsFromConsole(CapturedOutput output) {
        return Arrays.stream(output.getOut()
                                   .split("\n"))
                     .filter(v -> v.contains("--- "))
                     .map(v -> v.substring(v.lastIndexOf("--- ") + 4))
                     .collect(toList());
    }
}
