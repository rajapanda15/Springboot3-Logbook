package com.example.testproject;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import org.zalando.logbook.Logbook;
import org.zalando.logbook.netty.LogbookClientHandler;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@RestController
@Slf4j
public class MyController {

    @Autowired
    Logbook logbook;

    WebClient getWebClient(){

        HttpClient httpClient = HttpClient.create()
                                          .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 30_000)
                                          .responseTimeout(Duration.ofSeconds(30))
                                          .doOnConnected(connection -> connection.addHandlerFirst(new ReadTimeoutHandler(30, TimeUnit.SECONDS))
                                                                                 .addHandlerFirst(new WriteTimeoutHandler(30, TimeUnit.SECONDS))
                                                                                 .addHandlerLast(new LogbookClientHandler(logbook)));
        ReactorClientHttpConnector connector = new ReactorClientHttpConnector(httpClient);

        return WebClient.builder()
                        .baseUrl("https://jsonplaceholder.typicode.com")
                        .filter(logRequest())
                        .clientConnector(connector)
                        .build();
    }

    @GetMapping("/test")
    String test(){
        log.info("test log with tracing info");
        return getWebClient().get()
                             .uri("/todos/1")
                             .header(HttpHeaders.CONTENT_TYPE, "application/json")
                             .retrieve()
                             .bodyToMono(String.class)
                             .block();

    }
    ExchangeFilterFunction logRequest() {
        return ExchangeFilterFunction.ofRequestProcessor(clientRequest -> {
            if (log.isInfoEnabled()) {
                StringBuilder sb = new StringBuilder("Request: ");
                clientRequest
                        .headers()
                        .forEach((name, values) -> values.forEach(value -> sb.append(name+":"+value)));
                log.info(sb.toString());
            }
            return Mono.just(clientRequest);
        });
    }


}
