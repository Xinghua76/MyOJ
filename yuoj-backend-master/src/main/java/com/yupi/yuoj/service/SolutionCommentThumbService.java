package com.yupi.yuoj.service;

import com.yupi.yuoj.model.entity.SolutionCommentThumb;
import com.yupi.yuoj.model.entity.User;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * 题解评论点赞服务
 */
public interface SolutionCommentThumbService extends IService<SolutionCommentThumb> {

    /**
     * 点赞 / 取消点赞
     *
     * @param commentId
     * @param loginUser
     * @return
     */
    int doSolutionCommentThumb(long commentId, User loginUser);

    /**
     * 帖子评论点赞数内层互斥
     *
     * @param userId
     * @param commentId
     * @return
     */
    int doSolutionCommentThumbInner(long userId, long commentId);
}
