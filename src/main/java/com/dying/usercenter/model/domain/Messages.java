package com.dying.usercenter.model.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;


/**
 *
 * @TableName messages
 */
@TableName(value ="messages")
@Data
public class Messages implements Serializable {
    /**
     *
     */
    @TableId(type = IdType.AUTO)
    private Integer id;

    /**
     *
     */
    private String session_id;

    /**
     *
     */
    private String content;

    /**
     *
     */
    private Object role;

    /**
     *
     */
    private Date created_at;

    /**
     *
     */
    private Integer is_current;

    /**
     *
     */
    private Integer is_temp;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}