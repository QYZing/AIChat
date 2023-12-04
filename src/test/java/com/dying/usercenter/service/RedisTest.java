package com.dying.usercenter.service;
import java.util.Date;

import com.dying.usercenter.model.domain.User;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;


import javax.annotation.Resource;

@SpringBootTest
public class RedisTest {
    @Resource
    private RedisTemplate redisTemplate;

    @Test
    void test(){
        ValueOperations valueOperations = redisTemplate.opsForValue();
        //增
        valueOperations.set("dying" ,  "123");
        User user = new User();
        user.setUsername("user");
        user.setUserAccount("account");
        valueOperations.set("user" , user);
        //查
        Object dying = valueOperations.get("dying");
        Assertions.assertEquals("123", dying);
        Object user1 = valueOperations.get("user");
        Assertions.assertEquals(user , user1);
    }
}
