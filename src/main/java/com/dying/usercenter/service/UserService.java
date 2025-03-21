package com.dying.usercenter.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.dying.usercenter.model.domain.Users;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
* @author dying
* @description 用户服务
* @createDate 2023-11-09 20:14:54
*/
public interface UserService extends IService<Users> {
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
    Users userLogin(String userAccount , String userPassword , HttpServletRequest request);


    /**
     * 用户脱敏
     * @param originUser 原生user
     * @return
     */
    Users getSafetyUser(Users originUser);

    /**
     * 请求用户注销
     * @param request 请求对象
     * @return
     */
    int userLogout(HttpServletRequest request);

    /**
     * 根据标签搜索用户
     *
     * @param tagList 用户拥有的标签
     * @return
     */
    List<Users> searchUsersByTags(List<String> tagList);

    /**
     * 更新用户
     *
     * @param user 需要更新的用户信息
     * @param loginUser 当前登录的用户
     * @return
     */
    int updateUser(Users user , Users loginUser);

    /**
     * 获取当前登录用户信息
     * @return
     */
    Users getLoginUser(HttpServletRequest request);

    /**
     * 是否为管理员
     * @param request 请求
     * @return true 是
     */
    boolean isAdmin(HttpServletRequest request);


    /**
     * 是否为管理员
     * @param userLogin 用户
     * @return true 是
     */
    boolean isAdmin(Users userLogin);
}
