package com.nhn.webflux2021.configuration;

import com.nhn.webflux2021.reactive.r2dbc.Member;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

    @Bean
    public ReactiveRedisTemplate<String, Member> reactiveRedisTemplate(ReactiveRedisConnectionFactory contextFactory) {
        var key = new StringRedisSerializer();
        var value = new Jackson2JsonRedisSerializer<>(Member.class);

        var serializationContext = RedisSerializationContext.<String, Member>newSerializationContext()
                                                            .key(key)
                                                            .value(value)
                                                            .hashKey(key)
                                                            .hashValue(value)
                                                            .build();

        return new ReactiveRedisTemplate<>(contextFactory, serializationContext);
    }
}
