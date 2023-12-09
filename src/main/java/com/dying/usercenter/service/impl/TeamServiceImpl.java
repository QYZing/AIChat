package com.dying.usercenter.service.impl;
import java.util.Date;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dying.usercenter.common.ErrorCode;
import com.dying.usercenter.exception.BusinessException;
import com.dying.usercenter.mapper.TeamMapper;
import com.dying.usercenter.model.domain.Team;
import com.dying.usercenter.model.domain.User;
import com.dying.usercenter.model.domain.UserTeam;
import com.dying.usercenter.model.dto.TeamQuery;
import com.dying.usercenter.model.enums.TeamStatusEnum;
import com.dying.usercenter.model.request.TeamJoinRequest;
import com.dying.usercenter.model.request.TeamQuitRequest;
import com.dying.usercenter.model.request.TeamUpdateRequest;
import com.dying.usercenter.model.vo.TeamUserVO;
import com.dying.usercenter.model.vo.UserVO;
import com.dying.usercenter.service.TeamService;
import com.dying.usercenter.service.UserService;
import com.dying.usercenter.service.UserTeamService;
import com.github.xiaoymin.knife4j.core.util.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;

/**
* @author 86198
* @description 针对表【team(队伍)】的数据库操作Service实现
* @createDate 2023-12-04 21:37:35
*/
@Service
public class TeamServiceImpl extends ServiceImpl<TeamMapper, Team>
    implements TeamService{

    @Resource
    private UserTeamService userTeamService;

    @Resource
    private UserService userService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public long addTeam(Team team, User loginUser) {
        if(team == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        if(loginUser == null){
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }

        final long userId = loginUser.getId();

        // 1 队伍人数 <= 20 > 1
        int maxNum = Optional.ofNullable(team.getMaxNum()).orElse(0);
        if(maxNum < 1 || maxNum > 20){
            throw new BusinessException(ErrorCode.PARAMS_ERROR , "队伍标题不满足要求");
        }

        //2 队伍标题 <= 20
        String name = team.getName();
        if(StringUtils.isBlank(name) || name.length() > 20){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍标题不满足要求");
        }

        //3 描述 <= 512
        String description = team.getDescription();
        if(StringUtils.isNotBlank(description) && description.length() > 512){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍描述过长");
        }

        //4 status 是否公开（int) 不穿默认为0（公开）
        int status = Optional.ofNullable(team.getStatus()).orElse(0);
        TeamStatusEnum teamStatusEnum = TeamStatusEnum.getEnumByValue(status);
        if( teamStatusEnum == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍状态 不满足要求");
        }

        //5 如果status是加密状态，要有密码 密码 <= 32
        String password = team.getPassword();
        if(TeamStatusEnum.SECRET.equals(teamStatusEnum)){
            if(StringUtils.isBlank(password) || password.length() > 32){
                throw new BusinessException(ErrorCode.PARAMS_ERROR , "密码设置不正确");
            }
        }

        // 6 超时时间 > 当前时间
        Date expireTime = team.getExpireTime();
        if(new Date().after(expireTime)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR , "超时时间 > 当前时间");
        }

        // 7 校验用户最多5个队伍
        //todo 有bug 可能同时创建100
        QueryWrapper<Team> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userId", userId);
        long hasTeamNum = this.count(queryWrapper);
        if(hasTeamNum >= 5){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户最多创建5个用户");
        }

        //8 插入到队伍表
        team.setId(null);
        team.setUserId(userId);
        boolean save = this.save(team);
        Long teamId = team.getId();
        if(!save || teamId == null){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR , "创建队伍失败");
        }


        //9 插入用户 => 队伍关系到关系表
        UserTeam userTeam = new UserTeam();
        userTeam.setUserId(userId);
        userTeam.setTeamId(teamId);
        userTeam.setJoinTime(new Date());
        save = userTeamService.save(userTeam);
        if(!save){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR , "创建队伍失败");
        }
        return teamId;
    }

    @Override
    public List<TeamUserVO> listTeams(TeamQuery teamQuery, boolean isAdmin) {
        QueryWrapper<Team> queryWrapper = new QueryWrapper<>();
        //组合查询条件
        if(teamQuery != null){
            Long id = teamQuery.getId();
            if(id != null && id > 0){
                queryWrapper.eq("id" ,id);
            }
            List<Long> idList = teamQuery.getIdList();
            if(CollectionUtils.isNotEmpty(idList)){
                queryWrapper.in("id" , idList);
            }
            String searchText = teamQuery.getSearchText();
            if(StringUtils.isNotBlank(searchText)){
                queryWrapper.and(qw -> qw.like("name" , searchText).or().like("description" , searchText));
            }
            String name = teamQuery.getName();
            if(StringUtils.isNotBlank(name)){
                queryWrapper.like("name" , name);
            }
            String description = teamQuery.getDescription();
            if(StringUtils.isNotBlank(description)){
                queryWrapper.like("description" , description);
            }
            //查询最大人数
            Integer maxNum = teamQuery.getMaxNum();
            if(maxNum != null && maxNum > 0){
                queryWrapper.eq("maxNum" , maxNum);
            }
            Long userId = teamQuery.getUserId();
            //根据创建人来查询
            if(userId != null && userId > 0){
                queryWrapper.eq("userId" ,userId);
            }
            //根据状态查询
            Integer status = teamQuery.getStatus();
            TeamStatusEnum enumByValue = TeamStatusEnum.getEnumByValue(status);
            if(enumByValue == null){
                enumByValue = TeamStatusEnum.PUBLIC;
            }
            if(!isAdmin && !enumByValue.equals(TeamStatusEnum.PUBLIC)){
                throw new BusinessException(ErrorCode.NO_AUTH);
            }
            queryWrapper.eq("status" , enumByValue.getValue());

        }
        //不展示以过期队伍
        queryWrapper.and(qw -> qw.gt("expireTime" , new Date()).or().isNull("expireTime"));
        List<Team> list = this.list(queryWrapper);
        if(CollectionUtils.isEmpty(list)){
            return new ArrayList<>();
        }
        List<TeamUserVO> teamUserVOList = new ArrayList<>();
        // 关联查询创建人的用户信息
        for(Team team : list){
            Long userId = team.getUserId();
            if(userId == null){
                continue;
            }
            User user = userService.getById(userId);
            TeamUserVO teamUserVO = new TeamUserVO();
            BeanUtils.copyProperties(team , teamUserVO);
            //脱敏用户信息
            if(user != null){
                UserVO userVO = new UserVO();
                BeanUtils.copyProperties(user, userVO);
                teamUserVO.setCreateUser(userVO);
            }
            teamUserVOList.add(teamUserVO);
        }
        return teamUserVOList;
    }

    @Override
    public boolean updateTeam(TeamUpdateRequest teamUpdateRequest , User loginUser) {
        if(teamUpdateRequest == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Long id = teamUpdateRequest.getId();
        Team oldTeam = getTeamById(id);
        //只有管理员和队长更新
        if(!Objects.equals(oldTeam.getUserId(), loginUser.getId()) && !userService.isAdmin(loginUser)){
            throw new BusinessException(ErrorCode.NO_AUTH);
        }
        TeamStatusEnum statusEnum = TeamStatusEnum.getEnumByValue(teamUpdateRequest.getStatus());
        //todo 加密-》加密有问题
        if(statusEnum.equals(TeamStatusEnum.SECRET) && StringUtils.isBlank(teamUpdateRequest.getPassword())){
            throw new BusinessException(ErrorCode.PARAMS_ERROR , "加密状态下一定要有密码");
        }
        Team team = new Team();
        BeanUtils.copyProperties(teamUpdateRequest , team);
        return this.updateById(team);
    }

    @Override
    public boolean joinTeam(TeamJoinRequest teamJoinRequest , User loginUser) {
        if(teamJoinRequest == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Long teamId = teamJoinRequest.getTeamId();
        Team team = getTeamById(teamId);
        if(team.getExpireTime() != null && team.getExpireTime().before(new Date())){
            throw new BusinessException(ErrorCode.NULL_ERROR  , "队伍已过期");
        }
        Integer status = team.getStatus();
        TeamStatusEnum teamStatusEnum = TeamStatusEnum.getEnumByValue(status);
        if(TeamStatusEnum.PRIVATE.equals(teamStatusEnum)){
            throw new BusinessException(ErrorCode.NULL_ERROR  , "禁止加入私有队伍");
        }
        String password = teamJoinRequest.getPassword();
        if(TeamStatusEnum.SECRET.equals(teamStatusEnum)){
            if(StringUtils.isBlank(password) || !password.equals(team.getPassword())){
                throw new BusinessException(ErrorCode.NULL_ERROR  , "密码错误");
            }
        }
        //该用户已加入的队伍数量
        Long userId = loginUser.getId();
        QueryWrapper<UserTeam> userTeamQueryWrapper = new QueryWrapper<>();
        userTeamQueryWrapper.eq("userId" , userId);
        long count = userTeamService.count(userTeamQueryWrapper);
        if(count > 5){
            throw new BusinessException(ErrorCode.PARAMS_ERROR , "最多创建或加入5个队伍");
        }
        //不能重复加入已加入的队伍
        userTeamQueryWrapper = new QueryWrapper<>();
        userTeamQueryWrapper.eq("userId" , userId);
        userTeamQueryWrapper.eq("teamId" , teamId);
        long HasJoinNumTeam = userTeamService.count(userTeamQueryWrapper);
        if(HasJoinNumTeam > 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户已加入");
        }
        //已加入队伍的人数
        long teamHasJoinNum = this.countTeamUserByTeamId(teamId);
        if(teamHasJoinNum > team.getMaxNum()){
            throw new BusinessException(ErrorCode.PARAMS_ERROR , "队伍已满");
        }
        //修改队伍信息
        UserTeam userTeam = new UserTeam();
        userTeam.setUserId(userId);
        userTeam.setTeamId(teamId);
        userTeam.setJoinTime(new Date());
        return userTeamService.save(userTeam);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean quitTeam(TeamQuitRequest teamQuitRequest, User loginUser) {
        if(teamQuitRequest == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Long teamId = teamQuitRequest.getTeamId();
        Team team = getTeamById(teamId);
        Long userId = loginUser.getId();
        UserTeam queryUserTeam = new UserTeam();
        queryUserTeam.setTeamId(teamId);
        queryUserTeam.setUserId(userId);
        QueryWrapper<UserTeam> queryWrapper = new QueryWrapper<>(queryUserTeam);
        long count = userTeamService.count(queryWrapper);
        if (count == 0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR , "未加入队伍");
        }
        long teamHasJoinNum = countTeamUserByTeamId(teamId);
        //队伍只剩一人解散队伍
        if(teamHasJoinNum == 1){
            //删除队伍
            this.removeById(teamId);
        }else{
            //队伍至少2人
            //是队长
            if(Objects.equals(team.getUserId(), userId)){
                //把队伍庄毅给最早加入的用户
                //1 查询已加入队伍的所有用户和加入时间
                QueryWrapper<UserTeam> userTeamQueryWrapper = new QueryWrapper<>();
                userTeamQueryWrapper.eq("teamId" , teamId);
                userTeamQueryWrapper.last("order by id asc limit 2");
                List<UserTeam> userTeamlist = userTeamService.list(userTeamQueryWrapper);
                if(CollectionUtils.isEmpty(userTeamlist) || userTeamlist.size() <= 1){
                    throw new BusinessException(ErrorCode.SYSTEM_ERROR);
                }
                UserTeam nextUserTeam = userTeamlist.get(1);
                Long nextUserTeamId = nextUserTeam.getId();
                //更新当前队伍队长
                Team updateTeam = new Team();
                updateTeam.setId(teamId);
                updateTeam.setUserId(nextUserTeamId);
                boolean result = this.updateById(updateTeam);
                if (!result){
                    throw new BusinessException(ErrorCode.SYSTEM_ERROR,"更新队长失败");
                }

            }
        }
        //移除关系
        return userTeamService.remove(queryWrapper);
    }

    private Team getTeamById(Long teamId) {
        if (teamId == null || teamId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Team team = this.getById(teamId);
        if (team == null) {
            throw new BusinessException(ErrorCode.NULL_ERROR, "队伍不存在");
        }
        return team;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteTeam(long teamId, User loginUser) {
        //检验队伍是否存在
        Team team = getTeamById(teamId);
        //检验是不是队长
        if(!team.getUserId().equals(loginUser.getId())){
            throw new BusinessException(ErrorCode.NO_AUTH);
        }
        //移除所有加入队伍的关联信息
        QueryWrapper<UserTeam> userTeamQueryWrapper = new QueryWrapper<>();
        userTeamQueryWrapper.eq("teamId" , teamId);
        boolean result = userTeamService.remove(userTeamQueryWrapper);
        if (!result){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR , "删除队伍关联信息失败");
        }
        // 删除队伍
        return this.removeById(teamId);
    }

    private long countTeamUserByTeamId(long teamId){
        QueryWrapper<UserTeam> userTeamQueryWrapper = new QueryWrapper<>();
        userTeamQueryWrapper.eq("teamId" , teamId);
        return userTeamService.count(userTeamQueryWrapper);
    }
}




