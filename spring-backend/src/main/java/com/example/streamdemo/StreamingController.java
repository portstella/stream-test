package com.example.streamdemo;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import java.util.Map;

@RestController
public class StreamingController {

    private final WebClient pythonWebClient;

    // 通过构造函数注入WebClient Bean
    public StreamingController(WebClient pythonWebClient) {
        this.pythonWebClient = pythonWebClient;
    }

    @PostMapping(value = "/api/stream", produces = MediaType.TEXT_PLAIN_VALUE)
    public Flux<String> streamFromPython(@RequestBody Map<String, String> requestBody) {
        String prompt = requestBody.get("prompt");

        // 使用WebClient调用Python FastAPI服务
        return pythonWebClient.post()
                .uri("/stream") // Python服务的endpoint
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Map.of("prompt", prompt)) // 设置请求体
                .retrieve() // 发起请求并获取响应
                .bodyToFlux(org.springframework.core.io.buffer.DataBuffer.class) // 1. 将响应体转换为DataBuffer流
                .map(dataBuffer -> {
                    byte[] bytes = new byte[dataBuffer.readableByteCount()];
                    dataBuffer.read(bytes);
                    org.springframework.core.io.buffer.DataBufferUtils.release(dataBuffer);
                    String s = new String(bytes);
                    System.out.print(s);
                    return s; // 2. 将每个DataBuffer解码为字符串
                })
                .doOnCancel(() -> System.out.println("<UNK>"))
                .doOnComplete(()-> System.out.println())  ;
    }
}


