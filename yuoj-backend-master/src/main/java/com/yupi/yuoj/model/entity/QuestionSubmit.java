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
 * Question submit
 */
@TableName(value = "question_submit")
@Data
public class QuestionSubmit implements Serializable {

    /**
     * id
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * language
     */
    private String language;

    /**
     * code
     */
    private String code;

    /**
     * judge info json
     */
    private String judgeInfo;

    /**
     * status
     */
    private Integer status;

    /**
     * question id
     */
    private Long questionId;

    /**
     * contest id
     */
    private Long contestId;

    /**
     * user id
     */
    private Long userId;

    /**
     * submit time
     */
    private Date submitTime;

    /**
     * update time
     */
    private Date updateTime;

    /**
     * deleted flag
     */
    @TableLogic
    private Integer isDelete;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
