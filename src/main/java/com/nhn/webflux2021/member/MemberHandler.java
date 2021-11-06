package com.nhn.webflux2021.member;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.Part;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.util.ResourceUtils;
import org.springframework.web.reactive.function.BodyExtractors;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ServerWebInputException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.io.BufferedReader;
import java.io.FileReader;
import java.nio.charset.Charset;
import java.time.Duration;
import java.util.Arrays;
import java.util.Objects;

@Component
public class MemberHandler {
    final Logger log = LoggerFactory.getLogger(this.getClass());

    public Mono<ServerResponse> getMember(ServerRequest request) {
        request.headers()
               .asHttpHeaders()
               .toSingleValueMap()
               .forEach((key, value) -> log.info("{} : {}", key, value));

        log.info(request.queryParam("reactive")
                        .orElse("no queryParam"));

        return ServerResponse.ok()
                             .bodyValue(request.pathVariable("id"));
    }

    public Mono<ServerResponse> createMember(ServerRequest request) {
        var memberMono = request.bodyToMono(Member.class)
                                .map(member -> {
                                    member.setPhone("031-0101-0101");

                                    return member;
                                });

        return ServerResponse.ok()
                             .body(memberMono, Member.class);
    }

    public Mono<ServerResponse> upload(ServerRequest request) {
        Mono<MultiValueMap<String, Part>> body = request.body(BodyExtractors.toMultipartData());

        return body.flatMap(parts -> {
            Part file = parts.getFirst("upload.log");

            log.info("파일명 : {}", Objects.requireNonNull(file).name());

            var flux = file.content()
                           .flatMap(buf -> {
                               String received = buf.toString(Charset.defaultCharset());
                               return Flux.fromStream(Arrays.stream(received.split("\n")));
                           })
                           .buffer(100)
                           .delayElements(Duration.ofMillis(500))
                           .log()
                           .flatMapSequential(lists -> Mono.just(lists.stream()
                                                                      .map(Integer::parseInt)
                                                                      .reduce(Integer::sum)
                                                                      .orElse(0))
                           );

            return ServerResponse.ok()
                                 .contentType(MediaType.TEXT_EVENT_STREAM)
                                 .body(flux, Integer.class);
        });
    }

    public Mono<ServerResponse> getAddresses(ServerRequest request) {
        String keyword = request.queryParam("keyword")
                                .orElseThrow();
        String pageNumber = request.queryParam("pageNumber")
                                   .orElseThrow();
        String pageSize = request.queryParam("pageSize")
                                 .orElseThrow();
        String clientId = request.headers()
                                 .firstHeader("clientId");
        String platform = request.headers()
                                 .firstHeader("platform");

        return WebClient.create("https://alpha-shop-api.e-ncp.com/")
                        .get()
                        .uri("/addresses/search?pageNumber={pageNumber}&pageSize={pageSize}&keyword={keyword}", pageNumber, pageSize, keyword)
                        .header("clientId", clientId)
                        .header("platform", platform)
                        .accept(MediaType.APPLICATION_JSON)
                        .retrieve() // vs exchange
                        .onStatus(HttpStatus::is4xxClientError, response -> Mono.error(new ServerWebInputException("input error")))
                        .onStatus(HttpStatus::is5xxServerError, response -> Mono.error(new ServerWebInputException("input error")))
                        .bodyToMono(String.class)
                        .flatMap(body -> ServerResponse.ok()
                                                       .contentType(MediaType.APPLICATION_JSON)
                                                       .bodyValue(body));
    }

    /**
     * @see <a href="https://projectreactor.io/docs/core/release/reference/#faq.wrap-blocking">wrap-blocking</a>
     */
    public Mono<ServerResponse> blocking(ServerRequest request) {
        return Mono.fromCallable(this::findOne)
                   .subscribeOn(Schedulers.boundedElastic())
                   .as(body -> ServerResponse.ok()
                                             .body(body, String.class));
    }

    String findOne() throws Exception {
        FileReader fileReader = new FileReader(ResourceUtils.getFile("classpath:blocking.log"));
        BufferedReader bufferedReader = new BufferedReader(fileReader);

        return bufferedReader.readLine();
    }

    public static class Member {
        public String id;
        public String name;
        public String phone;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getPhone() {
            return phone;
        }

        public void setPhone(String phone) {
            this.phone = phone;
        }
    }
}
