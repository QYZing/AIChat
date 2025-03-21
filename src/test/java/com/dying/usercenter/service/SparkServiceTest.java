package com.dying.usercenter.service;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;


@SpringBootTest
public class SparkServiceTest {

    @Resource
    private SparkService sparkManager;


    private final String userInput = "你是谁";

    @Test
    public void testApi() {
        String result = sparkManager.sendHttpTOSpark(userInput);
        System.out.println(result);
    }
}