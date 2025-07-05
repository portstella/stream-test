package com.example.streamdemo;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Bean
    public WebClient pythonWebClient() {
        // 配置WebClient以指向我们的Python服务
        return WebClient.builder()
                .baseUrl("http://localhost:8000") // Python服务地址
                .build();
    }
}
