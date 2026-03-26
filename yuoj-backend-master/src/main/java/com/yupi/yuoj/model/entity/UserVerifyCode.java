package com.yupi.yuoj.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

@TableName(value = "user_verify_code")
@Data
public class UserVerifyCode implements Serializable {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    @TableField("user_id")
    private Long userId;

    @TableField("email")
    private String email;

    @TableField("phone")
    private String phone;

    @TableField("code")
    private String code;

    /**
     * register / login / reset
     */
    @TableField("scene")
    private String scene;

    /**
     * 0 unused / 1 used / 2 expired
     */
    @TableField("status")
    private Integer status;

    @TableField("expire_time")
    private Date expireTime;

    @TableField("create_time")
    private Date createTime;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
