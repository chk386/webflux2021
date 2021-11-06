package com.nhn.webflux2021.configuration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.kafka.core.reactive.ReactiveKafkaProducerTemplate;
import reactor.core.publisher.Sinks;
import reactor.kafka.sender.SenderOptions;

import java.nio.ByteBuffer;

@Configuration
public class KafkaConfig {
    final Logger log = LoggerFactory.getLogger(this.getClass());
    final KafkaProperties kafkaProperties;
    final Sinks.Many<String> multicast;

    public KafkaConfig(KafkaProperties kafkaProperties, Sinks.Many<String> multicast) {
        this.kafkaProperties = kafkaProperties;
        this.multicast = multicast;
    }

    @Bean
    public ReactiveKafkaProducerTemplate<String, String> producer() {
        return new ReactiveKafkaProducerTemplate<>(SenderOptions.create(kafkaProperties.buildProducerProperties()));
    }

    @Bean
    ApplicationRunner run() {
        return args -> DataBufferUtils.readInputStream(() -> System.in, new DefaultDataBufferFactory(), 1024)
                                      .map(dataBuffer -> {
                                          ByteBuffer byteBuffer = dataBuffer.asByteBuffer();
                                          return "Server -> " + new String(byteBuffer.array(), byteBuffer.arrayOffset(), byteBuffer.capacity());
                                      })
                                      .doOnNext(log::debug)
                                      .doOnNext(input -> producer().send("NHN", input)
                                                                   .subscribe())
                                      .subscribe(multicast::tryEmitNext);
    }
}
