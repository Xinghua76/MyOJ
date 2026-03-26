package com.yupi.yuoj.model.dto.user;

import java.io.Serializable;
import lombok.Data;

/**
 * User create request
 */
@Data
public class UserAddRequest implements Serializable {

    private String userName;
    private String userAccount;
    private String userAvatar;
    private String userRole;
    private String userPassword;

    private static final long serialVersionUID = 1L;
}
