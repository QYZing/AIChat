package com.dying.usercenter.service;

import org.junit.jupiter.api.Test;
import org.redisson.api.RList;
import org.redisson.api.RedissonClient;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@SpringBootTest
public class RedissonTest {

    @Resource
    private RedissonClient redissonClient;

    @Test
    void test(){
        //list 数据存在本地JVM中
        List<String> list = new ArrayList<>();
        list.add("dying");
        System.out.println("list: " + list.get(0));

        list.remove(0);

        //数据在redis 的内存中
        RList<String> rList = redissonClient.getList("test-list");
        rList.add("dying");
        System.out.println("rlist:" + rList.get(0));

        rList.remove(0);
    }
}
