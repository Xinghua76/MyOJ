package com.yupi.yuoj.model.dto.postcomment;

import com.yupi.yuoj.common.PageRequest;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 帖子评论查询请求
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class PostCommentQueryRequest extends PageRequest implements Serializable {

    /**
     * id
     */
    private Long id;

    /**
     * 帖子 id
     */
    private Long postId;

    /**
     * 创建用户 id
     */
    private Long userId;

    /**
     * 父评论 id
     */
    private Long parentId;

    /**
     * 状态
     */
    private Integer status;

    private static final long serialVersionUID = 1L;
}