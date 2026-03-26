package com.yupi.yuoj.model.dto.user;

import lombok.Data;
import java.io.Serializable;

@Data
public class UserRegisterByPhoneRequest implements Serializable {
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
     * 密码
     */
    private String userPassword;

    /**
     * 确认密码
     */
    private String checkPassword;
}
