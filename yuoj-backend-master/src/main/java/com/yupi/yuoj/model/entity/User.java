package com.yupi.yuoj.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

@TableName(value = "user")
@Data
public class User implements Serializable {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    @TableField("user_account")
    private String userAccount;

    @TableField("user_password")
    private String userPassword;

    @TableField("user_name")
    private String userName;

    @TableField("user_avatar")
    private String userAvatar;

    @TableField("user_profile")
    private String userProfile;

    @TableField("email")
    private String email;

    @TableField("phone")
    private String phone;

    @TableField("gender")
    private Integer gender;

    @TableField("user_role")
    private String userRole;

    @TableField("status")
    private Integer status;

    @TableField("last_login_time")
    private Date lastLoginTime;

    @TableField("create_time")
    private Date createTime;

    @TableField("update_time")
    private Date updateTime;

    @TableLogic
    @TableField("is_delete")
    private Integer isDelete;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}

