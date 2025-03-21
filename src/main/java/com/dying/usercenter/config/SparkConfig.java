package com.dying.usercenter.config;

import io.github.briqt.spark4j.SparkClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SparkConfig {

    @Value("${spark.ai.app-id}")
    private String appId;

    @Value("${spark.ai.api-key}")
    private String apiKey;

    @Value("${spark.ai.api-secret}")
    private String apiSecret;

    @Value("${spark.ai.host}")
    private String hostUrl;

    @Bean
    public SparkClient sparkAIClient() {
        SparkClient sparkClient = new SparkClient();
        sparkClient.appid = this.appId;
        sparkClient.apiSecret = this.apiSecret;
        sparkClient.apiKey = this.apiKey;
        return sparkClient;
    }
}