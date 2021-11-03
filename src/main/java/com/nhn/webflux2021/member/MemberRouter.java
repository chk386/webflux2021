package com.nhn.webflux2021.member;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
public class MemberRouter {
    final Logger log = LoggerFactory.getLogger(this.getClass());
    final MemberHandler memberHandler;

    public MemberRouter(MemberHandler memberHandler) {
        this.memberHandler = memberHandler;
    }

    @Bean
    public RouterFunction<ServerResponse> memberRoute() {
        return route().before(this::before)
                      .path("/members",
                            b -> b.GET("/{id}", memberHandler::getMember)
                                  .POST("", memberHandler::createMember)
                      )
                      .POST("/upload", memberHandler::upload)
                      .GET("/addresses", memberHandler::getAddresses)
                      .after(this::after)
                      .build();
    }

    private ServerRequest before(ServerRequest req) {
//        log.info("Before >>>> 접속 IP : {}", req.localAddress()
//                                              .orElseThrow()
//                                              .getAddress()
//                                              .getHostAddress());

        return req;
    }

    private ServerResponse after(ServerRequest req, ServerResponse res) {
        log.info("After >>>> 응답 : {}", res.statusCode()
                                          .getReasonPhrase());
        return res;
    }
}
