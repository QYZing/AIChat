package com.dying.usercenter.service.impl;

import cn.xfyun.api.IatClient;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dying.usercenter.constant.ChatRoleConstant;
import com.dying.usercenter.mapper.SessionsMapper;
import com.dying.usercenter.model.domain.Messages;
import com.dying.usercenter.model.domain.Sessions;
import com.dying.usercenter.model.dto.HistorySessionDTO;
import com.dying.usercenter.model.dto.MessageDTO;
import com.dying.usercenter.model.request.ChatRequest;
import com.dying.usercenter.model.request.SpeakRequest;
import com.dying.usercenter.model.response.ChatResponse;
import com.dying.usercenter.service.MessagesService;
import com.dying.usercenter.mapper.MessagesMapper;
import com.dying.usercenter.service.SparkService;
import com.dying.usercenter.service.UserService;
import com.dying.usercenter.utils.CustomSparkListener;
import com.dying.usercenter.utils.FfmpegUtil;
import com.dying.usercenter.utils.SpeechUtils;
import com.github.xiaoymin.knife4j.core.util.CommonUtils;
import io.github.briqt.spark4j.SparkClient;
import io.github.briqt.spark4j.constant.SparkApiVersion;
import io.github.briqt.spark4j.exception.SparkException;
import io.github.briqt.spark4j.listener.SparkBaseListener;
import io.github.briqt.spark4j.model.SparkMessage;
import io.github.briqt.spark4j.model.request.SparkRequest;
import io.github.briqt.spark4j.model.request.function.SparkRequestFunctionMessage;
import io.github.briqt.spark4j.model.response.SparkResponse;
import okhttp3.Response;
import okhttp3.WebSocket;
import org.apache.logging.log4j.message.Message;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.HttpRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.publisher.Flux;

import javax.annotation.Resource;
import java.beans.Encoder;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.SignatureException;
import java.time.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;


/**
* @author 86198
* @description 针对表【messages】的数据库操作Service实现
* @createDate 2025-03-17 17:20:31
*/
@Service
public class MessagesServiceImpl extends ServiceImpl<MessagesMapper, Messages>
    implements MessagesService{

    @Resource
    private SparkService sparkService;

    @Resource
    private  SparkClient sparkClient;

    @Resource
    private IatClient iatClient;
    @Resource
    private SessionsMapper sessionRepo;
    @Resource
    private MessagesMapper messageRepo;

    // 普通处理
    public ChatResponse normalProcess(ChatRequest request, int userId) {
        Sessions session = initSession(userId, request);
        saveMessage(session.getId(), request.getMessage(), ChatRoleConstant.USER);

        String aiResponse = sparkService.sendHttpTOSpark(request.getMessage());
        saveMessage(session.getId() , aiResponse,ChatRoleConstant.AI);
        ChatResponse response = new ChatResponse();
        response.setIsCompleate(Boolean.TRUE);
        response.setContent(aiResponse);
        response.setSessionId(session.getId());

        return response;
    }

    private Sessions initSession(int userId, ChatRequest request) {
        if (StringUtils.isNotBlank(request.getSessionId())) {
            return sessionRepo.selectById(request.getSessionId());
        }
        Sessions session = new Sessions();
        session.setId(UUID.randomUUID().toString());
        session.setUser_id(userId);
        session.setAssistant_type(request.getAssistantType());
        session.setCreated_at(getNow());
        sessionRepo.insert(session);
        return session;
    }

    private Messages saveMessage(String sessionId, String content , String Role) {
        Messages message = new Messages();
        message.setSession_id(sessionId);
        message.setContent(content);
        message.setRole(Role);
        message.setCreated_at(getNow());
        messageRepo.insert(message);
        return message;
    }

    private Date getNow(){
        // 1. 创建一个 LocalDateTime 对象
        LocalDateTime localDateTime = LocalDateTime.now();
        System.out.println("LocalDateTime: " + localDateTime);

        // 2. 指定时区（例如系统默认时区）
        ZoneId zoneId = ZoneId.systemDefault();

        // 3. 将 LocalDateTime 转换为 ZonedDateTime（带时区）
        ZonedDateTime zonedDateTime = localDateTime.atZone(zoneId);

        // 4. 转换为 Instant
        Instant instant = zonedDateTime.toInstant();

        // 5. 转换为 Date
        Date date = Date.from(instant);
        System.out.println("Date: " + date);
        return  date;
    }


    private Messages getOrCreateAssistantMessage(String sessionId) {
        Messages lastMsg = messageRepo.selectOne(new QueryWrapper<Messages>()
                .eq("session_id", sessionId)
                .eq("role", "assistant")
                .orderByDesc("create_time")
                .last("LIMIT 1"));

        if (lastMsg == null) {
            Messages message = new Messages();
            message.setSession_id(sessionId);
            message.setRole("assistant");
            message.setContent("");
            message.setCreated_at(getNow());
            messageRepo.insert(message);
            return message;
        }
        return lastMsg;
    }


    public SseEmitter processStream(ChatRequest request, int userId) {
        SseEmitter emitter = new SseEmitter(180_000L); // 3分钟超时

        // 1. 初始化会话
        Sessions session = initSession(userId, request);

        // 2. 保存用户消息
        saveMessage(session.getId(), request.getMessage() , ChatRoleConstant.USER);

        // 3. 构建历史上下文
        List<SparkMessage> history = buildHistoryContext(userId , request.getAssistantType());

        // 4. 创建SDK请求
        SparkRequest sparkRequest = SparkRequest.builder()
                .messages(history)
                .maxTokens(2048)
                .temperature(0.7)
                .apiVersion(SparkApiVersion.V4_0)
                .build();

        // 5. 创建自定义监听器
        CustomSparkListener listener = new CustomSparkListener(
                emitter,
                session.getId(),
                messageRepo
        );
        listener.setSparkRequest(sparkRequest);

        // 6. 发起流式请求
        sparkClient.chatStream(sparkRequest, listener);

        // 7. 设置完成回调
        emitter.onCompletion(() -> {
            sessionRepo.updateById(session);
        });

        return emitter;
    }

    @Override
    public SseEmitter processSpeakStream(SpeakRequest request, int userId) {
        SseEmitter emitter = new SseEmitter(180_000L); // 3分钟超时
        InputStream inputStream  = null;
        try {
            inputStream = request.getAudioFIle().getInputStream();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        System.out.println(request.getAudioFIle().getSize() );
        try {
            if("audio/webm; codecs=opus".equals(request.getAudioFIle().getContentType())){
                String outPath = FfmpegUtil.convertAudio(request.getAudioFIle());
                inputStream = Files.newInputStream(Paths.get(outPath));
            }

        }catch (Exception e){}

        // 1. 初始化会话
        ChatRequest chatRequest = new ChatRequest();
        chatRequest.setAssistantType(request.getAssistantType());
        Sessions session = initSession(userId, chatRequest);

        SpeechUtils sp = new SpeechUtils(iatClient ,emitter , session.getId() , messageRepo);
        try {
            sp.transcribeAudio(inputStream);
        } catch (SignatureException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return emitter;
    }

    private List<SparkMessage> buildHistoryContext(int userId , String Role) {
        List<String> ses =sessionRepo.selectIdByUA(userId , Role);
        // 1. 获取用户所有会话
        List<SparkMessage> res = new ArrayList<>();

        for (String se : ses) {
            List<Messages> msg = messageRepo.getRecentMessages(se, 2);
            for(Messages me : msg) {
                SparkMessage spm = new SparkMessage(
                        "user".equals(me.getRole()) ? ChatRoleConstant.USER : ChatRoleConstant.AI,
                        me.getContent()
                );
                res.add(spm);
            }
        }

        return res;
    }

    private void sendChunk(String content, Sessions session, SseEmitter emitter) {
        try {
            emitter.send(SseEmitter.event()
                    .id(UUID.randomUUID().toString())
                    .name("message")
                    .data(content)
            );

            session.setCreated_at(new Date());
            // 更新会话活跃时间
            sessionRepo.updateById(session);
        } catch (IOException e) {
            throw new RuntimeException("SSE发送失败", e);
        }
    }

    private void sendCompletion(Messages message, SseEmitter emitter) {
        try {
            emitter.send(SseEmitter.event()
                    .name("complete")
                    .data(Map.of(
                            "messageId", message.getId(),
                            "sessionId", message.getSession_id()
                    ))
            );
            emitter.complete();
        } catch (IOException e) {
            log.error("完成事件发送失败", e);
        }
    }

    private void handleError( Sessions session, SseEmitter emitter) {
        SparkException e = new SparkException(40000 , "ai出错");
        log.error("星火API调用失败 [错误码:"  +  e.getCode() + "{}]\"", e);

        try {
            // 1. 发送错误事件
            emitter.send(SseEmitter.event()
                    .name("error")
                    .data(Map.of(
                            "code",e.getCode(),
                    "message" , "AI服务暂时不可用"
            ))
        );

            // 2. 保存错误日志
            Messages errorMsg = new Messages();
            errorMsg.setSession_id(session.getId());
            errorMsg.setContent("服务错误: " + e.getMessage());
            errorMsg.setRole(ChatRoleConstant.USER);
            messageRepo.insert(errorMsg);


        } catch (IOException ex) {
            log.error("错误事件发送失败", ex);
        } finally {
            emitter.complete();
        }
    }

    private List<SparkMessage> buildMessages(List<Message> history, ChatRequest request) {
        return Stream.concat(
                history.stream()
                        .map(msg -> new SparkMessage("", msg.getFormattedMessage())),
                Stream.of(new SparkMessage("user", request.getMessage()))
        ).collect(Collectors.toList());
    }
}




