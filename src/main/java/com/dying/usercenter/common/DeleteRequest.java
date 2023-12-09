package com.dying.usercenter.common;

import lombok.Data;

import java.io.Serializable;

/**
 * 通用删除请求参数
 *
 * @author dying
 */
@Data
public class DeleteRequest implements Serializable {
    private long id;
}
