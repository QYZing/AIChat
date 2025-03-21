package com.dying.usercenter.config;

import cn.xfyun.api.IatClient;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class XfyunConfig {
    @Value("${spark.ai.app-id}")
    private String appId;

    @Value("${spark.ai.api-key}")
    private String apiKey;

    @Value("${spark.ai.api-secret}")
    private String apiSecret;

    @Value("${spark.ai.host}")
    private String hostUrl;
    @Bean
    public IatClient iatClient() {
        return new IatClient.Builder()
                .signature(appId, apiKey, apiSecret)
                .build();
    }
}