package com.dying.usercenter.controller;


import com.dying.usercenter.common.BaseResponse;
import com.dying.usercenter.common.ResultUtils;
import com.dying.usercenter.model.domain.Users;
import com.dying.usercenter.model.dto.HistorySessionDTO;
import com.dying.usercenter.model.request.ChatHistoryRequest;
import com.dying.usercenter.model.request.ChatRequest;
import com.dying.usercenter.model.request.SpeakRequest;
import com.dying.usercenter.model.response.ChatResponse;
import com.dying.usercenter.service.HistoryService;
import com.dying.usercenter.service.MessagesService;
import com.dying.usercenter.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.publisher.Flux;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/chat")
@Slf4j
public class ChatMessageController {
    @Resource
    private  MessagesService chatService;

    @Resource
    private UserService userService;

    @Resource
    private HistoryService historyService;


    @PostMapping(value = "speak", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter handleVoiceInput(@RequestParam("audio") MultipartFile audioFile,
                                       @RequestParam("assistantType") String assistantType,
                                       HttpServletRequest httpServletRequest){
        // 1. 用户认证
        int userId = userService.getLoginUser(httpServletRequest).getId();

        SpeakRequest request = new SpeakRequest();
        request.setAssistantType(assistantType);
        request.setAudioFIle(audioFile);
        // 4. 启动处理流程
        return chatService.processSpeakStream(request, userId);

    }

    @PostMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamChat(
            @RequestBody ChatRequest request,
            HttpServletRequest httpServletRequest
    ) {
        // 1. 用户认证
        int userId = userService.getLoginUser(httpServletRequest).getId();

        // 4. 启动处理流程
        return chatService.processStream(request, userId);
    }

    // 普通对话接口（非流式）
    @PostMapping(value = "/normal")
    public BaseResponse<ChatResponse> normalChat(
            @RequestBody ChatRequest request
            , HttpServletRequest httpRequest
    ) {
        int userId = userService.getLoginUser(httpRequest).getId();
        return ResultUtils.success(chatService.normalProcess(request ,  userId));
    }


    // UserController.java
    @GetMapping("/history")
    public BaseResponse<List<HistorySessionDTO>> getHistory(String assistantType , HttpServletRequest request) {
        // 1. 获取当前用户
        Users currentUser = userService.getLoginUser(request);

        // 2. 查询历史记录
        List<HistorySessionDTO> history = historyService.getChatHistoryByAssistant(assistantType , currentUser.getId());

        return ResultUtils.success(history);
    }
}
