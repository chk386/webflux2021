package com.nhn.webflux2021.reactive.mongo;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document
public class MemberHistory {

    @Id
    private Integer memberId;
    private LocalDateTime createdAt;

    public Integer getMemberId() {
        return memberId;
    }

    public void setMemberId(Integer memberId) {
        this.memberId = memberId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "MemberHistory{" +
                "memberId=" + memberId +
                ", createdAt=" + createdAt +
                '}';
    }
}
