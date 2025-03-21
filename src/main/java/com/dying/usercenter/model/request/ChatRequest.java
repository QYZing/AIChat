package com.dying.usercenter.model.request;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * 用户登录请求体
 *
 * @author dying
 */
@Data
public class ChatRequest implements Serializable {
    @NotBlank
    private String message;
    private String sessionId; // 可选会话ID
    @NotBlank
    private String assistantType; // 对应前端侧边栏类型
}
