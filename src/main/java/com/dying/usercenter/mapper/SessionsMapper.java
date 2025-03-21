package com.dying.usercenter.mapper;

import com.dying.usercenter.model.domain.Sessions;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
* @author 86198
* @description 针对表【sessions】的数据库操作Mapper
* @createDate 2025-03-17 17:20:37
* @Entity com.dying.usercenter.model.domain.Sessions
*/
public interface SessionsMapper extends BaseMapper<Sessions> {
    @Select("SELECT * FROM sessions WHERE user_id = #{userId} ORDER BY created_at DESC")
    List<Sessions> selectByUserId(@Param("userId") Integer userId);

    @Select("SELECT id FROM sessions WHERE user_id = #{userId} and assistant_type = #{assistant_type}")
    List<String> selectIdByUA(@Param("userId") Integer userId , @Param("assistant_type") String Role);
}




