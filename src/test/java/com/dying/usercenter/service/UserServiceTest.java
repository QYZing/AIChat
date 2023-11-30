package com.dying.usercenter.service;


import com.dying.usercenter.model.domain.User;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

/***
 * 用户读物测试
 *
 * @author dying
 */
@SpringBootTest
class UserServiceTest {

    @Resource
    private UserService userService;

    @Test
    public void testAdduser(){
        User user = new User();
        user.setUsername("dying");
        user.setUserAccount("123");
        user.setAvatarUrl("xxx");
        user.setGender(0);
        user.setUserPassword("123");
        user.setPhone("123");
        user.setEmail("456");
        boolean result = userService.save(user);
        System.out.println(user.getId());
        Assertions.assertTrue(result);
    }

    @Test
    void userRegister() {
        String userAccount = "dyingTest";
        String userPassword = "";
        String checkPassword = "12345678";
        //非空
        long result = userService.userRegister(userAccount , userPassword , checkPassword);
        Assertions.assertEquals(-1 , result);
        //账户长度不小于4位
        userAccount = "dy";
        userPassword = "12345678";
        result = userService.userRegister(userAccount , userPassword , checkPassword);
        Assertions.assertEquals(-1 , result);
        //密码不小于8位
        userAccount = "dyingTest";
        userPassword = "123456";
        result = userService.userRegister(userAccount , userPassword , checkPassword);
        Assertions.assertEquals(-1 , result);
        //账户不能重复
        userAccount = "dying";
        userPassword = "12345678";
        result = userService.userRegister(userAccount , userPassword , checkPassword);
        Assertions.assertEquals(-1 , result);
        //账户不能包含特殊字符
        userAccount = "dying test";
        result = userService.userRegister(userAccount , userPassword , checkPassword);
        Assertions.assertEquals(-1 , result);
        // 密码和校验密码相同
        userAccount = "dyingTest";
        checkPassword = "123456789";
        result = userService.userRegister(userAccount , userPassword , checkPassword);
        Assertions.assertEquals(-1 , result);
        //正确逻辑
        checkPassword = "12345678";
        result = userService.userRegister(userAccount , userPassword , checkPassword);
        Assertions.assertTrue(result > 0);
    }
}