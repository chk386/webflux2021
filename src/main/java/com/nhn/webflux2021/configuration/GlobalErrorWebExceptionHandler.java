package com.nhn.webflux2021.configuration;

import org.springframework.boot.autoconfigure.web.WebProperties;
import org.springframework.boot.autoconfigure.web.reactive.error.AbstractErrorWebExceptionHandler;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.reactive.error.ErrorAttributes;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.*;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Map;

@Component
@Order(-2) // 스프링 기본 에러처리 order는 -1이다.
public class GlobalErrorWebExceptionHandler extends AbstractErrorWebExceptionHandler {
    public GlobalErrorWebExceptionHandler(ErrorAttributes errorAttributes, WebProperties.Resources resources, ApplicationContext applicationContext, ServerCodecConfigurer configurer) {
        super(errorAttributes, resources, applicationContext);
        this.setMessageWriters(configurer.getWriters());
        this.setMessageReaders(configurer.getReaders());
    }

    @Override
    protected RouterFunction<ServerResponse> getRoutingFunction(ErrorAttributes errorAttributes) {
        return RouterFunctions.route(RequestPredicates.all(), this::renderErrorResponse);
    }

    private Mono<ServerResponse> renderErrorResponse(ServerRequest request) {

        final Map<String, Object> errorPropertiesMap = getErrorAttributes(request, ErrorAttributeOptions.defaults());
        int status = (int)errorPropertiesMap.get("status");

        var error = switch (status) {
            case 404 -> "찾을수 없는 페이지입니다.";
            case 500 -> "서버에러입니다.";
            default -> "알수없는 에러입니다.";
        };

        errorPropertiesMap.put("globalErrorMessage", error);

        return ServerResponse.status(HttpStatus.BAD_REQUEST)
                             .contentType(MediaType.APPLICATION_JSON)
                             .body(BodyInserters.fromValue(errorPropertiesMap));
    }
}

