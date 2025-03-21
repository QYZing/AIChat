package com.dying.usercenter.service.impl;

import com.dying.usercenter.service.SparkService;
import io.github.briqt.spark4j.SparkClient;
import io.github.briqt.spark4j.constant.SparkApiVersion;
import io.github.briqt.spark4j.model.SparkMessage;
import io.github.briqt.spark4j.model.SparkSyncChatResponse;
import io.github.briqt.spark4j.model.request.SparkRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;


@Service
@Slf4j
public class SparkServiceImpl implements SparkService {

    @Resource
    private SparkClient sparkClient;

    /**
     * AI生成问题的预设条件
     */
    public static final String PRECONDITION = "在这里填写对Spark的预设：\n" +
            "这里可以添加角色预设，譬如说“你是一名摄影师”\n" +
            "给我一些摄影方面的指导\n" +
            "这里可以添加对ai回答格式或方向的指定";

    @Override
    public String sendHttpTOSpark(String content) {
        // 消息列表，可以在此列表添加历史对话记录
        List<SparkMessage> messages = new ArrayList<>();
        messages.add(SparkMessage.systemContent(PRECONDITION));
        messages.add(SparkMessage.userContent(content));
        // 构造请求
        SparkRequest sparkRequest = SparkRequest.builder()
                // 消息列表
                .messages(messages)
                // 指定请求版本，lite版本是v1.5
                .apiVersion(SparkApiVersion.V1_5)
                .build();
        // 同步调用
        SparkSyncChatResponse chatResponse = sparkClient.chatSync(sparkRequest);
        String responseContent = chatResponse.getContent();
        System.out.println("Spark AI 返回的结果{}" + responseContent);
        return responseContent;
    }
}
