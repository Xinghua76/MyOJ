package com.yupi.yuoj.model.dto.solutioncommentthumb;

import java.io.Serializable;
import lombok.Data;

/**
 * 题解评论点赞请求
 */
@Data
public class SolutionCommentThumbAddRequest implements Serializable {

    /**
     * 评论 id
     */
    private Long commentId;

    private static final long serialVersionUID = 1L;
}
