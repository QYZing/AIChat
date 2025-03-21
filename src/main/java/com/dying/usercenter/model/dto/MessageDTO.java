package com.dying.usercenter.model.dto;

import lombok.Data;

import java.util.Date;

@Data
public class MessageDTO {
    private String content;
    private String role; // user/assistant
    private Date createdAt;
}