package com.nhn.webflux2021.member;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.Part;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.BodyExtractor;
import org.springframework.web.reactive.function.BodyExtractors;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.charset.Charset;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static java.lang.String.join;

@Component
public class MemberHandler {
    final Logger log = LoggerFactory.getLogger(this.getClass());

    // localhost:8080/members/nhn?reactive=webflux
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
            Part file = parts.getFirst("file");
            log.info("업로드된 파일명 : {}", file.name());

            var flux = file.content()
                           .flatMap(buf -> {
                               String received = buf.toString(Charset.defaultCharset());
                               return Flux.fromStream(Arrays.stream(received.split("\n")));
                           })
                           .buffer(100)
                           .delayElements(Duration.ofMillis(500))
                           .log();


            return ServerResponse.ok()
                                 .contentType(MediaType.TEXT_EVENT_STREAM)
                                 .body(flux, String.class);
        });
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
