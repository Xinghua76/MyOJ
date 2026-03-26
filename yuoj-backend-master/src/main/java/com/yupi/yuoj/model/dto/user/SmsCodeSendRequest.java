package com.yupi.yuoj.model.dto.user;

import lombok.Data;
import java.io.Serializable;

@Data
public class SmsCodeSendRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 场景：register / login / reset
     */
    private String scene;
}
