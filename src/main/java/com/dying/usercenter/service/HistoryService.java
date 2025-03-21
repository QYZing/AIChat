package com.dying.usercenter.service;

import com.dying.usercenter.model.dto.HistorySessionDTO;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface HistoryService {
    List<HistorySessionDTO> getChatHistory(Integer userId);

    List<HistorySessionDTO> getChatHistoryByAssistant(String assistantType , int userId);
}
