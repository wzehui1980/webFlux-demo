package com.wzh.demo.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

  @Value("${app.base-url:http://localhost:8080}")
  private String baseUrl;

  @Bean
  public WebClient webClient() {
    return WebClient.builder()
        .baseUrl(baseUrl + "/api")
        .build();
  }
}
