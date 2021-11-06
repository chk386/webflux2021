package com.nhn.webflux2021.configuration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.web.reactive.HandlerMapping;
import org.springframework.web.reactive.handler.SimpleUrlHandlerMapping;
import org.springframework.web.reactive.socket.WebSocketHandler;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

import java.time.Duration;
import java.util.Map;
import java.util.Objects;

@Configuration
public class WebSocketConfig {
    Logger log = LoggerFactory.getLogger(this.getClass());

    final ReactiveMongoTemplate reactiveMongoTemplate;
    final Sinks.Many<String> multicast;

    public WebSocketConfig(ReactiveMongoTemplate reactiveMongoTemplate, Sinks.Many<String> multicast) {
        this.reactiveMongoTemplate = reactiveMongoTemplate;
        this.multicast = multicast;
    }

    @Bean
    public HandlerMapping handlerMapping() {
        var urlMap = Map.of("/chat", chatHandler()
                , "/other", chatHandler());
        var order = -1; // before annotated controllers

        return new SimpleUrlHandlerMapping(urlMap, order);
    }

    WebSocketHandler chatHandler() {
        return session -> {
            final var ip = Objects.requireNonNull(session.getHandshakeInfo()
                                                         .getRemoteAddress())
                                  .getAddress()
                                  .getHostAddress();

            var input = session.receive()
                               .map(message -> new Chat(ip, message.getPayloadAsText()))
                               .doOnNext(chat -> log.debug("메세지 : {}, IP : {}", chat.message, chat.ip))
                               .doOnNext(chat -> multicast.tryEmitNext(chat.toString()))
                               .bufferTimeout(10, Duration.ofSeconds(5))
                               .doOnNext(chats -> reactiveMongoTemplate.insert(chats, "chats")
                                                                       .subscribe())
                               .then();

            var output = session.send(multicast.asFlux()
                                               .map(session::textMessage));

            return Mono.zip(input, output)
                       .then();
        };
    }

    @Document
    static record Chat(String ip, String message) {
    }
}
