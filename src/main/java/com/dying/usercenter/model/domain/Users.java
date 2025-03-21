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
 * @TableName users
 */
@TableName(value ="users")
@Data
public class Users implements Serializable {
    /**
     *
     */
    @TableId(type = IdType.AUTO)
    private Integer id;

    /**
     *
     */
    private String username;

    /**
     *
     */
    private String userPassword;

    /**
     *
     */
    private Date created_at;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}