package com.dying.usercenter.model.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 
 * @TableName sessions
 */
@TableName(value ="sessions")
@Data
public class Sessions implements Serializable {
    /**
     * 
     */
    @TableId
    private String id;

    /**
     * 
     */
    private Integer user_id;

    /**
     * 
     */
    private String assistant_type;

    /**
     * 
     */
    private Date created_at;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}