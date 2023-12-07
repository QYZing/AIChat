package com.dying.usercenter.service;

import com.dying.usercenter.model.domain.Team;
import com.baomidou.mybatisplus.extension.service.IService;
import com.dying.usercenter.model.domain.User;
import com.dying.usercenter.model.dto.TeamQuery;
import com.dying.usercenter.model.request.TeamJoinRequest;
import com.dying.usercenter.model.request.TeamUpdateRequest;
import com.dying.usercenter.model.vo.TeamUserVO;

import java.util.List;

/**
* @author 86198
* @description 针对表【team(队伍)】的数据库操作Service
* @createDate 2023-12-04 21:37:35
*/
public interface TeamService extends IService<Team> {

    /**
     * 创建队伍
     * @param team
     * @param loginUser
     * @return
     */
    long addTeam(Team team , User loginUser);

    /**
     * 搜索队伍
     *
     * @param teamQuery
     * @param isAdmin
     * @return
     */
    List<TeamUserVO> listTeams(TeamQuery teamQuery,boolean isAdmin);

    /**
     * 更新队伍
     * @param teamUpdateRequest
     * @param loginUser
     * @return
     */
    boolean updateTeam(TeamUpdateRequest teamUpdateRequest, User loginUser);

    boolean joinTeam(TeamJoinRequest teamJoinRequest, User loginUser);

}
