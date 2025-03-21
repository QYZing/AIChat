package com.dying.usercenter.utils;


import cn.xfyun.api.IatClient;
import cn.xfyun.model.response.iat.IatResponse;
import cn.xfyun.model.response.iat.IatResult;
import cn.xfyun.service.iat.AbstractIatWebSocketListener;
import com.dying.usercenter.constant.ChatRoleConstant;
import com.dying.usercenter.mapper.MessagesMapper;
import com.dying.usercenter.model.domain.Messages;
import okhttp3.Response;
import okhttp3.WebSocket;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import javax.validation.constraints.NotNull;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.security.SignatureException;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class SpeechUtils {

    private final SseEmitter emitter;
    private final String sessionId;
    private final MessagesMapper messageMapper;

    private final IatClient iatClient;



    private final StringBuilder fullContent = new StringBuilder();

    public SpeechUtils(IatClient iatClient , SseEmitter emitter, String sessionId, MessagesMapper messageMapper) {
        this.emitter = emitter;
        this.sessionId = sessionId;
        this.messageMapper = messageMapper;
        this.iatClient = iatClient;
    }

    public void transcribeAudio(InputStream audioFileStream) throws MalformedURLException, FileNotFoundException, SignatureException {
        iatClient.send(audioFileStream, new AbstractIatWebSocketListener() {
            @Override
            public void onSuccess(WebSocket webSocket, IatResponse response) {
                try {
                    handleResponse(response);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            public void onFail(WebSocket webSocket, Throwable t, Response response) {
                onFailure(webSocket , t , response);
            }
        });
    }

    private void handleResponse(IatResponse response) throws IOException {
        if (response.getCode() != 0) {
            onFailure(new Throwable(("code=>" + response.getCode() + " error=>" + response.getMessage() + " sid=" + response.getSid())));
            return;
        }

        if (response.getData() != null && response.getData().getResult() != null) {
            appendResultText(response.getData().getResult());
        }

        // resp.data.status ==2 说明数据全部返回完毕，可以关闭连接，释放资源
        if (response.getData().getStatus() == 2) {
            onComplete();
        }
    }

    private void appendResultText(IatResult result) throws IOException {
        IatResult.Ws[] wss = result.getWs();
        StringBuilder text = new StringBuilder();
        for (IatResult.Ws ws : wss) {
            IatResult.Cw[] cws = ws.getCw();

            for (IatResult.Cw cw : cws) {
                text.append(cw.getW());
            }
        }
        emitter.send(SseEmitter.event()
                .name("message")
                .data(text.toString()));

        // 累积完整内容
        fullContent.append(text);

        // 实时更新数据库
        messageMapper.upsertTempMessage(sessionId , text.toString());
    }

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
            iatClient.closeWebsocket();
        } catch (IOException e) {
//            log.error("完成事件发送失败", e);
        }
    }
    public void onFailure( @NotNull Throwable t) {
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