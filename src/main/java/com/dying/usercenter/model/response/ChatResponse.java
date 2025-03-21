package com.dying.usercenter.model.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ChatResponse {
    private String content;
    private String sessionId;
    private LocalDateTime timestamp;
    private Boolean isCompleate;
}
