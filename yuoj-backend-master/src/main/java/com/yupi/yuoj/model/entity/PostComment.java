package com.yupi.yuoj.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 帖子评论
 */
@TableName(value = "post_comment")
@Data
public class PostComment implements Serializable {

    /**
     * id
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 帖子 id
     */
    private Long postId;

    /**
     * 创建人 id
     */
    private Long userId;

    /**
     * 内容
     */
    private String content;

    /**
     * 父评论 id
     */
    private Long parentId;

    /**
     * 点赞数
     */
    private Integer thumbNum;

    /**
     * 状态（0-默认 1-已发布）
     */
    private Integer status;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 是否删除
     */
    @TableLogic
    private Integer isDelete;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}