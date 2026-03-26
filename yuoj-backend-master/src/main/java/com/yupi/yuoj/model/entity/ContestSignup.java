package com.yupi.yuoj.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * Contest signup
 */
@TableName(value = "contest_signup")
@Data
public class ContestSignup implements Serializable {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long contestId;

    private Long userId;

    private Date signupTime;

    private Integer status;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
