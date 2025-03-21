package com.dying.usercenter.mapper;

import com.dying.usercenter.model.domain.Messages;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dying.usercenter.model.dto.MessageDTO;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
* @author 86198
* @description 针对表【messages】的数据库操作Mapper
* @createDate 2025-03-17 17:20:31
* @Entity com.dying.usercenter.model.domain.Messages
*/
public interface MessagesMapper extends BaseMapper<Messages> {
    @Select("SELECT content, role, created_at FROM messages WHERE session_id = #{sessionId} ORDER BY created_at ASC")
    List<MessageDTO> selectMessagesBySession(@Param("sessionId") String sessionId);


//    @Select("SELECT content, role, created_at FROM messages WHERE session_id in ( #{sessionId} )ORDER BY created_at ASC")
//    List<MessageDTO> selectMessagesBySessions(@Param("sessionId") List<String> sessionId);

    // 临时消息更新
    @Update("INSERT INTO messages (session_id, content, role, is_temp) " +
            "VALUES (#{sessionId}, #{content}, 'assistant', 1) " +
            "ON DUPLICATE KEY UPDATE content = CONCAT(content, #{content})")
    void upsertTempMessage(@Param("sessionId") String sessionId,
                           @Param("content") String content);

    // 标记为正式消息
    @Update("UPDATE messages SET is_temp = 0 WHERE session_id = #{sessionId} AND role = 'assistant'")
    void finalizeMessage(@Param("sessionId") String sessionId);

    // 获取历史消息
    @Select("SELECT * FROM messages " +
            "WHERE session_id = #{sessionId} AND is_temp = 0 " +
            "ORDER BY created_at DESC LIMIT #{limit}")
    List<Messages> getRecentMessages(@Param("sessionId") String sessionId,
                                    @Param("limit") int limit);
}




