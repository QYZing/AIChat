package com.dying.usercenter.service;

import com.dying.usercenter.model.domain.Users;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
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
        Users user = new Users();
        user.setUsername("user");
        valueOperations.set("user" , user);
        //查
        Object dying = valueOperations.get("dying");
        Assertions.assertEquals("123", dying);
        Object user1 = valueOperations.get("user");
        Assertions.assertEquals(user , user1);
    }
}
