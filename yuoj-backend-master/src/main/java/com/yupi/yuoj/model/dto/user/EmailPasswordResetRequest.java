package com.yupi.yuoj.model.dto.user;

import java.io.Serializable;
import lombok.Data;

@Data
public class EmailPasswordResetRequest implements Serializable {

    private String email;

    private String code;

    private String newPassword;

    private String confirmPassword;

    private static final long serialVersionUID = 1L;
}
