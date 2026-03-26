package com.yupi.yuoj.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yupi.yuoj.model.entity.PostCommentThumb;
import com.yupi.yuoj.model.entity.User;

/**
 * 帖子评论点赞服务
 */
public interface PostCommentThumbService extends IService<PostCommentThumb> {

    /**
     * 点赞 / 取消点赞
     *
     * @param commentId
     * @param loginUser
     * @return
     */
    int doPostCommentThumb(long commentId, User loginUser);

    /**
     * 帖子评论点赞（内部服务）
     *
     * @param userId
     * @param commentId
     * @return
     */
    int doPostCommentThumbInner(long userId, long commentId);
}