package com.dying.usercenter.controller;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dying.usercenter.common.BaseResponse;
import com.dying.usercenter.common.ErrorCode;
import com.dying.usercenter.common.ResultUtils;
import com.dying.usercenter.exception.BusinessException;
import com.dying.usercenter.model.domain.User;
import com.dying.usercenter.model.request.UserLoginRequest;
import com.dying.usercenter.model.request.UserRegisterRequest;
import com.dying.usercenter.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.dying.usercenter.constant.UserConstant.USER_LOGIN_STATE;

/**
 * 用户接口
 *
 * @author dying
 */
// 这个注解返回的都是json restful风格api
@RestController
@RequestMapping("/user")
@Slf4j
public class UserController {
    @Resource
    private UserService userService;

    @Resource
    private RedisTemplate redisTemplate;

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
        if(!userService.isAdmin(request)){
            throw new BusinessException(ErrorCode.NO_AUTH , "不是管理员");
        }
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        if(!StringUtils.isNotBlank(username)){
            //模糊查询
            queryWrapper.like("username" , username);
        }
        List<User> userlist = userService.list(queryWrapper);
        List<User> result = userlist.stream().map(user -> {
            return userService.getSafetyUser(user);
        }).collect(Collectors.toList());
        return ResultUtils.success(result);
    }

    @GetMapping("/search/tags")
    public BaseResponse<List<User>> searchUsersByTags( @RequestParam(required = false)  List<String> tagNameList){
        if(CollectionUtils.isEmpty(tagNameList)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        List<User> userList = userService.searchUsersByTags(tagNameList);
        return ResultUtils.success(userList);
    }

    //todo
    @GetMapping("/recommend")
    public BaseResponse<Page<User>> recommendUsers(int pageSize , int pageNum , HttpServletRequest request){
        //TODO 整理到service中
        long userId = -1;
        try{
            userId = userService.getLoginUser(request).getId();
        }catch (BusinessException e){
            log.info("未登录");
        }
        String redisKey = String.format("partner:user:recommend:%s" , userId);
        ValueOperations valueOperations = redisTemplate.opsForValue();
        //如果有缓存，直接读缓存
        Page<User> userPage = (Page<User>) valueOperations.get(redisKey);
        if(userPage != null){
             return ResultUtils.success(userPage);
        }
        //无缓冲
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        userPage = userService.page(new Page<>(pageNum , pageSize) ,  queryWrapper);
        try {
            valueOperations.set(redisKey , userPage  , 30000 , TimeUnit.MICROSECONDS);
        } catch (Exception e) {
           log.error("redis set key error" , e);
        }
        return ResultUtils.success(userPage);
    }

    @PostMapping("/update")
    public BaseResponse<Integer> updateUser(@RequestBody User user , HttpServletRequest request){
        //校验参数是否为空
        if(user == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        //TODO 只有id其他没有时不允许更新
        User loginUser = userService.getLoginUser(request);
        int result = userService.updateUser(user , loginUser);
        return ResultUtils.success(result);
    }

    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteUsers(@RequestBody long id , HttpServletRequest request){
        //鉴权 仅管理员查询
        if(id <= 0 || !userService.isAdmin(request)){
            throw new BusinessException(ErrorCode.NO_AUTH , "无权限");
        }
        boolean result=userService.removeById(id);
        return ResultUtils.success(result);
    }


    /**
     * 获取最匹配的用户
     * @param num
     * @param request
     * @return
     */
    @GetMapping("/match")
    public BaseResponse<List<User>> matchUsers(long num, HttpServletRequest request){
        if(num <= 0 || num > 20){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        return ResultUtils.success(userService.matchUsers(num , loginUser));
    }


}
