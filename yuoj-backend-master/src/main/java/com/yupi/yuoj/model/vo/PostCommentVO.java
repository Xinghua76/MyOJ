package com.yupi.yuoj.model.vo;

import com.yupi.yuoj.model.entity.PostComment;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;
import org.springframework.beans.BeanUtils;

/**
 * 帖子评论视图
 */
@Data
public class PostCommentVO implements Serializable {

    private Long id;

    private Long postId;

    private Long userId;

    private String content;

    private Long parentId;

    private Integer thumbNum;

    private Integer status;

    private Date createTime;

    private Date updateTime;

    /**
     * 创建人信息
     */
    private UserVO user;

    /**
     * 是否已点赞
     */
    private Boolean hasThumb;

    public static PostCommentVO objToVo(PostComment postComment) {
        if (postComment == null) {
            return null;
        }
        PostCommentVO postCommentVO = new PostCommentVO();
        BeanUtils.copyProperties(postComment, postCommentVO);
        return postCommentVO;
    }

    private static final long serialVersionUID = 1L;
}