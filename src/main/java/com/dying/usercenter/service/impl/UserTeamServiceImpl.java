package com.dying.usercenter.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dying.usercenter.model.domain.UserTeam;
import com.dying.usercenter.service.UserTeamService;
import com.dying.usercenter.mapper.UserTeamMapper;
import org.springframework.stereotype.Service;

/**
* @author 86198
* @description 针对表【user_team(用户队伍关系)】的数据库操作Service实现
* @createDate 2023-12-04 21:38:40
*/
@Service
public class UserTeamServiceImpl extends ServiceImpl<UserTeamMapper, UserTeam>
    implements UserTeamService{

}




