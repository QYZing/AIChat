package com.dying.usercenter.service.impl;

import com.dying.usercenter.mapper.MessagesMapper;
import com.dying.usercenter.mapper.SessionsMapper;
import com.dying.usercenter.model.domain.Sessions;
import com.dying.usercenter.model.dto.HistorySessionDTO;
import com.dying.usercenter.model.dto.MessageDTO;
import com.dying.usercenter.service.HistoryService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class HistoryServiceImpl implements HistoryService {

    @Resource
    private SessionsMapper sessionsMapper;
    @Resource
    private MessagesMapper messagesMapper;
    @Override
    public List<HistorySessionDTO> getChatHistory(Integer userId) {
        // 1. 获取用户所有会话
        List<Sessions> sessions = sessionsMapper.selectByUserId(userId);

        return sessions.stream().map(session -> {
            // 2. 获取每个会话的消息
            List<MessageDTO> messages = messagesMapper.selectMessagesBySession(session.getId());

            // 3. 构建返回对象
            HistorySessionDTO dto = new HistorySessionDTO();
            dto.setSessionId(session.getId());
            dto.setAssistantType(session.getAssistant_type());
            dto.setCreatedAt(session.getCreated_at());
            dto.setMessages(messages);

            return dto;
        }).collect(Collectors.toList());
    }

    @Override
    public     List<HistorySessionDTO> getChatHistoryByAssistant(String assistantType , int userId){
        // 1. 获取用户所有会话
        List<String> sessions = sessionsMapper.selectIdByUA(userId , assistantType);

        return sessions.stream().map(session -> {
            // 2. 获取每个会话的消息
            List<MessageDTO> messages = messagesMapper.selectMessagesBySession(session);

            // 3. 构建返回对象
            HistorySessionDTO dto = new HistorySessionDTO();
            dto.setSessionId(session);
            dto.setAssistantType(assistantType);
//            dto.setCreatedAt(messages.);
            dto.setMessages(messages);

            return dto;
        }).collect(Collectors.toList());
    }
}
