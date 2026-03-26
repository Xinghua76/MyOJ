package com.yupi.yuoj.model.dto.user;

import java.io.Serializable;
import lombok.Data;

@Data
public class EmailCodeSendRequest implements Serializable {

    private String email;

    /**
     * register / login
     */
    private String scene;

    private static final long serialVersionUID = 1L;
}
