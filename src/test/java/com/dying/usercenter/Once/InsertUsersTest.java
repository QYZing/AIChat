package com.dying.usercenter.Once;

import com.dying.usercenter.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;


@SpringBootTest
public class InsertUsersTest {
    @Resource
    private UserService userService;

    /**
     * 批量插入用户
     */
//    @Scheduled(initialDelay = 5000 , fixedRate = Long.MAX_VALUE)
    @Test
    public void doInsertUser(){

    }
}
