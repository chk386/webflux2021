package com.nhn.webflux2021.reactive.mongo;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document
public record MemberHistory(@Id Integer memberId, LocalDateTime createdAt) {
}