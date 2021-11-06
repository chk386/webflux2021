package com.nhn.webflux2021.member;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.text.IsEqualIgnoringCase.equalToIgnoringCase;

@ContextConfiguration(classes = {MemberRouter.class, MemberHandler.class})
@WebFluxTest
class MemberRouterTest {

    @Autowired
    MemberHandler memberHandler;

    @Autowired
    MemberRouter memberRouter;

    @Autowired
    WebTestClient webTestClient;

    @Test
    @DisplayName("회원조회 테스트")
    void getMembers() {
        this.webTestClient
                .get()
                .uri("/members/{id}", "NHN")
                .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .exchange()
                .expectStatus()
                .isOk()
                .expectHeader()
                .contentTypeCompatibleWith(MediaType.TEXT_PLAIN)
                .expectBody(String.class)
                .consumeWith(response -> assertThat("응답은 NHN이여야 한다",
                                                    response.getResponseBody(),
                                                    equalToIgnoringCase("nhn"))
                );
    }
}