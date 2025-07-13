package com.example.streamdemo;

/**
 * 表示聊天消息的DTO（数据传输对象）。
 * 兼容OpenAI的格式。
 */
public class ChatMessage {
    private String role; // 角色，例如 "user" 或 "assistant"
    private String content; // 消息内容

    public ChatMessage() {
    }

    public ChatMessage(String role, String content) {
        this.role = role;
        this.content = content;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
