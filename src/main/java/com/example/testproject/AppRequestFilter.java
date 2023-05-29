package com.example.testproject;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.zalando.logbook.HttpHeaders;
import org.zalando.logbook.HttpRequest;
import org.zalando.logbook.RequestFilter;

@Component
@Slf4j
class AppRequestFilter implements RequestFilter {
  @Override
  public HttpRequest filter(HttpRequest request) {
    HttpHeaders headers = request.getHeaders();
    String cookie = headers.getFirst("Cookie");
    MDC.put("cookie",cookie);
    return request;
  }


}