package com.dying.usercenter.service;

import com.dying.usercenter.constant.ChatRoleConstant;
import com.dying.usercenter.model.domain.Messages;
import com.baomidou.mybatisplus.extension.service.IService;
import com.dying.usercenter.model.domain.Sessions;
import com.dying.usercenter.model.request.ChatRequest;
import com.dying.usercenter.model.request.SpeakRequest;
import com.dying.usercenter.model.response.ChatResponse;
import org.apache.logging.log4j.message.Message;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.security.SignatureException;
import java.time.Duration;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
* @author 86198
* @description 针对表【messages】的数据库操作Service
* @createDate 2025-03-17 17:20:31
*/
public interface MessagesService extends IService<Messages> {
    // 普通处理
    ChatResponse normalProcess(ChatRequest request, int userId) ;

    SseEmitter  processStream(ChatRequest request, int userId) ;

    SseEmitter processSpeakStream(SpeakRequest request, int userId) ;
}
