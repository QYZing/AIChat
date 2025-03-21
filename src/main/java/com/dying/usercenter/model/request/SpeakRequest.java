package com.dying.usercenter.model.request;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * 用户登录请求体
 *
 * @author dying
 */
@Data
public class SpeakRequest implements Serializable {
    @NotBlank
    private MultipartFile audioFIle;
    private String sessionId; // 可选会话ID
    @NotBlank
    private String assistantType; // 对应前端侧边栏类型
}
