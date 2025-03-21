package com.dying.usercenter.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dying.usercenter.common.ErrorCode;
import com.dying.usercenter.exception.BusinessException;
import com.dying.usercenter.mapper.UsersMapper;
import com.dying.usercenter.model.domain.Users;
import com.dying.usercenter.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.DigestUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.dying.usercenter.constant.UserConstant.USER_LOGIN_STATE;

/**
* @author dying
* @description 针对表【user】的数据库操作Service实现
* @createDate 2023-11-09 20:14:54
*/
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UsersMapper, Users>
    implements UserService{

    @Resource
    private UsersMapper userMapper;

    // 加盐
    private static final String SALT = "dying";

    @Override
    public long userRegister(String userAccount, String userPassword, String checkPassword) {
        //校验
        if(StringUtils.isAnyBlank(userAccount , userPassword , checkPassword)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR , "参数为空");
        }
        if(userAccount.length() < 4){
            throw new BusinessException(ErrorCode.PARAMS_ERROR , "用户账户小于4位");
        }
        if(userPassword.length() < 8 || checkPassword.length() < 8){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户密码过短");
        }
        //账户不能包含特殊字符
        String validPattern = "[`~!@#$%^&*()+=|{}':;',\\[\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]";
        Matcher matcher = Pattern.compile(validPattern).matcher((userAccount));
        if(matcher.find()){
            throw new BusinessException(ErrorCode.PARAMS_ERROR , "账户不能包含特殊字符");
        }
        //密码和校验密码相同
        if(!userPassword.equals(checkPassword)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR , "输入密码不一致");
        }
        //账户不能重复
        QueryWrapper<Users> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username" , userAccount);
        long count = userMapper.selectCount(queryWrapper);
        if(count > 0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账户重复");

        }
        //加密
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
        // 插入数据
        Users user = new Users();
        user.setUsername(userAccount);
        user.setUserPassword(encryptPassword);
        boolean result = this.save(user);
        // 不判断会返回null 而id是Long装箱类型，会出错
        if(!result){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        }
        return user.getId();
    }

    @Override
    public Users userLogin(String userAccount, String userPassword , HttpServletRequest request) {
        //校验
        if(StringUtils.isAnyBlank(userAccount , userPassword)){
            throw new BusinessException(ErrorCode.NULL_ERROR , "参数不完整");
        }
        if(userAccount.length() < 4){
            throw new BusinessException(ErrorCode.PARAMS_ERROR , "账号小于4位");
        }
        if(userPassword.length() < 8){
            throw new BusinessException(ErrorCode.PARAMS_ERROR , "密码小于8位");
        }
        //账户不能包含特殊字符
        String validPattern = "[`~!@#$%^&*()+=|{}':;',\\[\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]";
        Matcher matcher = Pattern.compile(validPattern).matcher((userAccount));
        if(matcher.find()){
            throw new BusinessException(ErrorCode.PARAMS_ERROR , "存在特殊字符");
        }
        //加密
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());

        //查询用户是否存在
        QueryWrapper<Users> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username" , userAccount);
        queryWrapper.eq("userPassword" , encryptPassword);
        Users user = userMapper.selectOne(queryWrapper);

        //用户不存在
        if(user == null){
            log.info("user login failed , userAccount cannot match userPassword");
            throw new BusinessException(ErrorCode. NO_INFO, "用户不存在");
        }

        //3 用户脱敏
        Users safetyUser = getSafetyUser(user);

        //4 记录用户登录态
        request.getSession().setAttribute(USER_LOGIN_STATE , safetyUser);
        return safetyUser;
    }

    /**
     * 用户脱敏
     * @param originUser 原生user
     * @return
     */
    @Override
    public Users getSafetyUser(Users originUser){
        if(originUser == null){
            return null;
        }
        Users safetyUser = new Users();
        safetyUser.setId(originUser.getId());
        safetyUser.setUsername(originUser.getUsername());
        return safetyUser;
    }

    /**
     * 用户注销
     * @param request 请求对象
     */
    @Override
    public int userLogout(HttpServletRequest request) {
        request.getSession().removeAttribute(USER_LOGIN_STATE);
        return 1;
    }


    /**
     * 根据标签搜索用户
     *
     * @param tagNameList 用户拥有的标签
     * @return
     */
    @Override
    public List<Users> searchUsersByTags(List<String> tagNameList){
        if(CollectionUtils.isEmpty(tagNameList)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        QueryWrapper<Users> queryWrapper = new QueryWrapper<>();
        for(String tagName : tagNameList){
            queryWrapper = queryWrapper.like("tags" , tagName);
        }
        List<Users> userList = userMapper.selectList(queryWrapper);
        return userList.stream().map(this::getSafetyUser).collect(Collectors.toList());
    }

    @Override
    public int updateUser(Users user, Users loginUser) {
        if(user == null || loginUser == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        long id = user.getId();
        if(id <= 0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        //仅管理员和自己可修改
        if(!isAdmin(loginUser) && id != loginUser.getId()){
            throw new BusinessException(ErrorCode.NO_AUTH);
        }
        Users oldUser = userMapper.selectById(id);
        if(oldUser == null){
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }
        return userMapper.updateById(user);
    }

    @Override
    public Users getLoginUser(HttpServletRequest request) {
        if(request == null){
            return null;
        }
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        if(userObj == null){
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }
        return (Users) userObj;
    }

    @Override
    public boolean isAdmin(HttpServletRequest request) {
        // 鉴权 仅管理员查询
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        Users user = (Users) userObj;
//        return user != null && user.getUserRole() == ADMIN_ROLE;
        return false;
    }

    @Override
    public boolean isAdmin(Users userLogin) {
//        return userLogin != null && userLogin.getUserRole() == ADMIN_ROLE;
        return false;
    }
}




