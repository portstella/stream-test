package com.example.streamdemo;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

@RestController
public class StreamingController {

    private final WebClient pythonWebClient;

    public StreamingController(WebClient pythonWebClient) {
        this.pythonWebClient = pythonWebClient;
    }

    /**
     * 接收来自前端的请求，并将其转发到后端的Python LLM服务。
     * 此端点模拟OpenAI的 /v1/chat/completions 接口，并以流式方式（SSE）返回结果。
     * @param request 聊天请求，格式与OpenAI兼容
     * @return 返回一个字符串的Flux流，每个字符串都是一个SSE事件
     */
    @PostMapping(value = "/v1/chat/completions", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> stream(@RequestBody ChatCompletionRequest request) {
        request.setStream(true); // 强制要求Python服务进行流式响应

        return pythonWebClient.post()
                .uri("/v1/chat/completions")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .retrieve()
                .bodyToFlux(String.class) // 将响应体作为原始字符串流直接转发
                .doOnNext(a->{
                    System.out.println(a);
                })
                .doOnError(throwable -> System.err.println("Error proxying stream: " + throwable.getMessage()));
    }
}

