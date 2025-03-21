package com.dying.usercenter.service;


import com.dying.usercenter.model.domain.Users;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;

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
        Users user = new Users();
        user.setUsername("dying");
        user.setUserPassword("123");
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

    @Test
    public void testSearchUsersByTags(){
        List<String> tagNameList = Arrays.asList("java" , "python");
        List<Users> userList = userService.searchUsersByTags(tagNameList);
        Assertions.assertNotNull(userList);
    }
}