package com.yupi.yuoj.model.dto.user;

import java.io.Serializable;
import lombok.Data;

@Data
public class EmailCodeLoginRequest implements Serializable {

    private String email;

    private String code;

    private static final long serialVersionUID = 1L;
}
