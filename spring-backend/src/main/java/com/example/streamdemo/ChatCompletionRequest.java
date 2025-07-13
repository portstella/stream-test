package com.example.streamdemo;

import java.util.List;

/**
 * 表示发送到聊天补全API的请求的DTO。
 * 兼容OpenAI的格式。
 */
public class ChatCompletionRequest {
    private String model; // 模型ID
    private List<ChatMessage> messages; // 消息列表
    private boolean stream = true; // 是否流式返回

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public List<ChatMessage> getMessages() {
        return messages;
    }

    public void setMessages(List<ChatMessage> messages) {
        this.messages = messages;
    }

    public boolean isStream() {
        return stream;
    }

    public void setStream(boolean stream) {
        this.stream = stream;
    }
}
