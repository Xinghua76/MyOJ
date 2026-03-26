package com.yupi.yuoj.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * Contest
 */
@TableName(value = "contest")
@Data
public class Contest implements Serializable {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private String title;

    private String description;

    private Date startTime;

    private Date endTime;

    private Integer status;

    private Long creatorId;

    private Date createTime;

    private Date updateTime;

    @TableLogic
    private Integer isDelete;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
