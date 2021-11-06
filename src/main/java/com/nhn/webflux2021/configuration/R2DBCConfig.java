package com.nhn.webflux2021.configuration;

import com.nhn.webflux2021.reactive.r2dbc.MemberReactiveRepository;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories;

@Configuration
@EnableR2dbcRepositories(basePackageClasses = MemberReactiveRepository.class)
public class R2DBCConfig {
}
