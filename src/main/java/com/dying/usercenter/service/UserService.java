package com.dying.usercenter.service;

import com.dying.usercenter.model.domain.User;
import com.baomidou.mybatisplus.extension.service.IService;

import javax.servlet.http.HttpServletRequest;

/**
* @author dying
* @description 用户服务
* @createDate 2023-11-09 20:14:54
*/
public interface UserService extends IService<User> {
    /**
     * 用户注册
     *
     * @param userAccount 用户账户
     * @param userPassword 用户密码
     * @param checkPassword 校验密码
     * @return 新用户 id
     */
    long userRegister(String userAccount , String userPassword , String checkPassword);

    /**
     * 用户登录
     * @param userAccount 用户账户
     * @param userPassword 用户密码
     * @return 脱敏用户信息
     */
    User userLogin(String userAccount , String userPassword , HttpServletRequest request);


    /**
     * 用户脱敏
     * @param originUser 原生user
     * @return
     */
    User getSafetyUser(User originUser);

    /**
     * 请求用户注销
     * @param request 请求对象
     * @return
     */
    int userLogout(HttpServletRequest request);
}
