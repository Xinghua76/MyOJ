package com.yupi.yuoj.model.dto.user;

import lombok.Data;
import java.io.Serializable;

@Data
public class UserResetPasswordByPhoneRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 验证码
     */
    private String code;

    /**
     * 新密码
     */
    private String newPassword;

    /**
     * 确认密码
     */
    private String confirmPassword;
}
