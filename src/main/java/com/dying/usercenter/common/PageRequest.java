package com.dying.usercenter.common;

import lombok.Data;

import java.io.Serializable;

/**
 * 分页参数
 *
 * @author dying
 */
@Data
public class PageRequest implements Serializable {
    private static final long serialVersionUID = -5191241716373120793L;

    /**
     * 页面大小
     */
    protected int pageSize = 10;
    /**
     * 当前是第几页
     */
    protected int pageNum = 1;
}
