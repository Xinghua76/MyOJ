package com.yupi.yuoj.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * Contest rank
 */
@TableName(value = "contest_rank")
@Data
public class ContestRank implements Serializable {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long contestId;

    private Long userId;

    private Integer solvedCount;

    private Integer totalScore;

    private Integer penalty;

    private Date updateTime;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
