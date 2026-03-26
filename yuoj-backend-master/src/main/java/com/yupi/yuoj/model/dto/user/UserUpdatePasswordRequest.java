package com.yupi.yuoj.model.dto.user;

import java.io.Serializable;
import lombok.Data;

/**
 * 用户更新密码请求
 */
@Data
public class UserUpdatePasswordRequest implements Serializable {

    /**
     * 旧密码
     */
    private String userPassword;

    /**
     * 新密码
     */
    private String newPassword;

    /**
     * 确认新密码
     */
    private String confirmPassword;

    private static final long serialVersionUID = 1L;
}
