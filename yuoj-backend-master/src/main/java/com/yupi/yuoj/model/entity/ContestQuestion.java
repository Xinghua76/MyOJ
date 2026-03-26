package com.yupi.yuoj.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import lombok.Data;

/**
 * Contest question
 */
@TableName(value = "contest_question")
@Data
public class ContestQuestion implements Serializable {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long contestId;

    private Long questionId;

    private Integer score;

    private Integer orderNo;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
