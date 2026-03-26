package com.yupi.yuoj.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

@TableName(value = "admin_op_log")
@Data
public class AdminOpLog implements Serializable {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private Long userId;
    private String opType;
    private String opDesc;
    private String opData;
    private Date createTime;
    private static final long serialVersionUID = 1L;
}
