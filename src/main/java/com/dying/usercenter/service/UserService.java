package com.dying.usercenter.service;

import com.dying.usercenter.common.BaseResponse;
import com.dying.usercenter.model.domain.User;
import com.baomidou.mybatisplus.extension.service.IService;
import com.dying.usercenter.model.vo.UserVO;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

import static com.dying.usercenter.constant.UserConstant.ADMIN_ROLE;
import static com.dying.usercenter.constant.UserConstant.USER_LOGIN_STATE;

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

    /**
     * 根据标签搜索用户
     *
     * @param tagList 用户拥有的标签
     * @return
     */
    List<User> searchUsersByTags(List<String> tagList);

    /**
     * 更新用户
     *
     * @param user 需要更新的用户信息
     * @param loginUser 当前登录的用户
     * @return
     */
    int updateUser(User user , User loginUser);

    /**
     * 获取当前登录用户信息
     * @return
     */
    User getLoginUser(HttpServletRequest request);

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
    boolean isAdmin(User userLogin);

    /**
     * 推荐用户
     * @param num
     * @param loginUser
     * @return
     */
    List<User> matchUsers(long num, User loginUser);

}
