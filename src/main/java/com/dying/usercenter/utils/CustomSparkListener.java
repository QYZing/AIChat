package com.dying.usercenter.utils;

import com.dying.usercenter.constant.ChatRoleConstant;
import com.dying.usercenter.mapper.MessagesMapper;
import com.dying.usercenter.model.domain.Messages;
import com.dying.usercenter.model.domain.Sessions;
import io.github.briqt.spark4j.listener.SparkBaseListener;
import io.github.briqt.spark4j.model.request.SparkRequest;
import io.github.briqt.spark4j.model.response.SparkResponse;
import io.github.briqt.spark4j.model.response.SparkResponseFunctionCall;
import io.github.briqt.spark4j.model.response.SparkResponseUsage;
import okhttp3.Response;
import okhttp3.WebSocket;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.util.Map;

public class CustomSparkListener extends SparkBaseListener {
    private final SseEmitter emitter;
    private final String sessionId;
    private final MessagesMapper messageMapper;
    private final StringBuilder fullContent = new StringBuilder();

    public CustomSparkListener(SseEmitter emitter, String sessionId, MessagesMapper messageMapper) {
        this.emitter = emitter;
        this.sessionId = sessionId;
        this.messageMapper = messageMapper;
    }

    @Override
    public void onMessage(String content, SparkResponseUsage usage, Integer status,
                          SparkRequest sparkRequest, SparkResponse sparkResponse,
                          WebSocket webSocket) {
        try {
            // 发送实时片段
            emitter.send(SseEmitter.event()
                    .name("message")
                    .data(content));

            // 累积完整内容
            fullContent.append(content);

            // 实时更新数据库
            messageMapper.upsertTempMessage(sessionId , content);
            if(status == 2){
                onComplete();;
            }

        } catch (IOException e) {
            throw new RuntimeException("SSE发送失败", e);
        }
    }

    // 新增完成回调方法

    public void onComplete() {
        try {
            // 最终保存完整消息
            messageMapper.finalizeMessage(sessionId);

            // 发送完成事件
            emitter.send(SseEmitter.event()
                    .name("complete")
                    .data(Map.of(
                            "sessionId", sessionId,
                            "contentLength", fullContent.length()
                    )));

            emitter.complete();
        } catch (IOException e) {
//            log.error("完成事件发送失败", e);
        }
    }
    @Override
    public void onFunctionCall(SparkResponseFunctionCall functionCall,
                               SparkResponseUsage usage, Integer status,
                               SparkRequest sparkRequest, SparkResponse sparkResponse,
                               WebSocket webSocket) {
        // 处理函数调用（根据业务需求实现）

    }

    @Override
    public void onFailure(@NotNull WebSocket webSocket, @NotNull Throwable t,
                          Response response) {
        // 保存错误日志
        saveErrorLog(t);

        try {
            emitter.send(SseEmitter.event()
                    .name("error")
                    .data(Map.of(
                            "code","API_ERROR",
                    "message", t.getMessage()
                )));
        } catch (IOException e) {
            System.out.println("错误事件发送失败"+ e.toString());
        }
    }


    private void saveErrorLog(Throwable t) {
        Messages errorMsg = new Messages();
        errorMsg.setSession_id(sessionId);
        errorMsg.setRole(ChatRoleConstant.AI);
        errorMsg.setContent("错误信息: " + t.getMessage());
        messageMapper.insert(errorMsg);
    }
}