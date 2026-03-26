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
 * Question
 */
@TableName(value = "question")
@Data
public class Question implements Serializable {

    /**
     * id
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * title
     */
    private String title;

    /**
     * content
     */
    private String content;

    /**
     * tags json
     */
    private String tags;

    /**
     * difficulty: 1/2/3
     */
    private Integer difficulty;

    /**
     * answer
     */
    private String answer;

    /**
     * submit count
     */
    private Integer submitNum;

    /**
     * accepted count
     */
    private Integer acceptedNum;

    /**
     * judge case json
     */
    private String judgeCase;

    /**
     * judge config json
     */
    private String judgeConfig;

    /**
     * like count
     */
    private Integer thumbNum;

    /**
     * favour count
     */
    private Integer favourNum;

    /**
     * creator id
     */
    private Long userId;

    /**
     * status: 1 on, 0 off
     */
    private Integer status;

    /**
     * create time
     */
    private Date createTime;

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
