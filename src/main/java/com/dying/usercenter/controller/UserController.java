package com.dying.usercenter.controller;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.dying.usercenter.common.BaseResponse;
import com.dying.usercenter.common.ErrorCode;
import com.dying.usercenter.common.ResultUtils;
import com.dying.usercenter.exception.BusinessException;
import com.dying.usercenter.model.domain.User;
import com.dying.usercenter.model.domain.request.UserLoginRequest;
import com.dying.usercenter.model.domain.request.UserRegisterRequest;
import com.dying.usercenter.service.UserService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.stream.Collectors;

import static com.dying.usercenter.constant.UserConstant.ADMIN_ROLE;
import static com.dying.usercenter.constant.UserConstant.USER_LOGIN_STATE;

/**
 * 用户接口
 *
 * @author dying
 */
// 这个注解返回的都是json restful风格api
@RestController
@RequestMapping("/user")
public class UserController {
    @Resource
    private UserService userService;
    @PostMapping("/register")
    public BaseResponse<Long> userRegister(@RequestBody UserRegisterRequest userRegisterRequest){
        if (userRegisterRequest == null){
             throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String userPassword = userRegisterRequest.getUserPassword();
        String userAccount = userRegisterRequest.getUserAccount();
        String checkPassword = userRegisterRequest.getCheckPassword();
        if(StringUtils.isAnyBlank(userAccount , userPassword , checkPassword)){
            throw new BusinessException(ErrorCode.NULL_ERROR , "输入参数为空");
        }
        long result =userService.userRegister(userAccount, userPassword, checkPassword);
        return ResultUtils.success(result);
    }

    @PostMapping("/login")
    public BaseResponse<User> userLogin(@RequestBody UserLoginRequest userLoginRequest , HttpServletRequest request){
        if (userLoginRequest == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR );
        }
        String userPassword = userLoginRequest.getUserPassword();
        String userAccount = userLoginRequest.getUserAccount();
        if(StringUtils.isAnyBlank(userAccount , userPassword )){
            throw new BusinessException(ErrorCode.NULL_ERROR , "输入参数为空");
        }
        User result =  userService.userLogin(userAccount, userPassword , request);
        return ResultUtils.success(result);
    }

    @PostMapping("/logout")
    public BaseResponse<Integer> userLogout(HttpServletRequest request){
        if (request == null){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        }
        int result = userService.userLogout(request);
        return ResultUtils.success(result);
    }

    @GetMapping("/current")
    public BaseResponse<User> getCurrentUser(HttpServletRequest request){
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        User currentUser = (User) userObj;
        if(currentUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN , "用户未登录");
        }
        long userId = currentUser.getId();
        //todo 校验用户是否合法
        User user = userService.getById(userId);
        User result = userService.getSafetyUser(user);
        return ResultUtils.success(result);
    }

    @GetMapping("/search")
    public BaseResponse<List<User>> searchUsers(String username , HttpServletRequest request){
        // 鉴权 仅管理员查询
        if(!isAdmin(request)){
            throw new BusinessException(ErrorCode.NO_AUTH , "不是管理员");
        }
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        if(StringUtils.isNotBlank(username)){
            //模糊查询
            queryWrapper.like("username" , username);
        }
        List<User> userlist = userService.list(queryWrapper);
        List<User> result = userlist.stream().map(user -> {
            return userService.getSafetyUser(user);
        }).collect(Collectors.toList());
        return ResultUtils.success(result);
    }


    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteUsers(@RequestBody long id , HttpServletRequest request){
        //鉴权 仅管理员查询
        if(id <= 0 || !isAdmin(request)){
            throw new BusinessException(ErrorCode.NO_AUTH , "无权限");
        }
        boolean result=userService.removeById(id);
        return ResultUtils.success(result);
    }

    /**
     * 是否为管理员
     * @param request 请求
     * @return true 是
     */
    private boolean isAdmin(HttpServletRequest request){
        // 鉴权 仅管理员查询
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        User user = (User) userObj;
        return user != null && user.getUserRole() == ADMIN_ROLE;
    }
}
