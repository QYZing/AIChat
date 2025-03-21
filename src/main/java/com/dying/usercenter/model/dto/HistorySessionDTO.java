package com.dying.usercenter.model.dto;

import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class HistorySessionDTO {
    private String sessionId;
    private String assistantType;
    private Date createdAt;
    private List<MessageDTO> messages;
}