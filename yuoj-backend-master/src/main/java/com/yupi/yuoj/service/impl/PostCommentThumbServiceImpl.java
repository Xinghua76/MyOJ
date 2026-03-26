package com.yupi.yuoj.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yupi.yuoj.common.ErrorCode;
import com.yupi.yuoj.exception.BusinessException;
import com.yupi.yuoj.mapper.PostCommentMapper;
import com.yupi.yuoj.mapper.PostCommentThumbMapper;
import com.yupi.yuoj.model.entity.PostComment;
import com.yupi.yuoj.model.entity.PostCommentThumb;
import com.yupi.yuoj.model.entity.User;
import com.yupi.yuoj.service.PostCommentService;
import com.yupi.yuoj.service.PostCommentThumbService;
import javax.annotation.Resource;
import org.springframework.aop.framework.AopContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 帖子评论点赞服务实现
 */
@Service
public class PostCommentThumbServiceImpl extends ServiceImpl<PostCommentThumbMapper, PostCommentThumb>
        implements PostCommentThumbService {

    @Resource
    private PostCommentService postCommentService;

    @Resource
    private PostCommentMapper postCommentMapper;

    @Override
    public int doPostCommentThumb(long commentId, User loginUser) {
        // 判断实体是否存在，根据类别获取实体
        PostComment postComment = postCommentService.getById(commentId);
        if (postComment == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        // 是否已点赞
        long userId = loginUser.getId();
        // 每个用户串行点赞
        // 锁必须要包裹住事务方法
        PostCommentThumbService postCommentThumbService = (PostCommentThumbService) AopContext.currentProxy();
        synchronized (String.valueOf(userId).intern()) {
            return postCommentThumbService.doPostCommentThumbInner(userId, commentId);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int doPostCommentThumbInner(long userId, long commentId) {
        PostCommentThumb postCommentThumb = new PostCommentThumb();
        postCommentThumb.setUserId(userId);
        postCommentThumb.setCommentId(commentId);
        QueryWrapper<PostCommentThumb> thumbQueryWrapper = new QueryWrapper<>(postCommentThumb);
        PostCommentThumb oldPostCommentThumb = this.getOne(thumbQueryWrapper);
        boolean result;
        // 已点赞
        if (oldPostCommentThumb != null) {
            result = this.remove(thumbQueryWrapper);
            if (result) {
                // 点赞数 - 1
                result = postCommentService.update()
                        .setSql("thumb_num = thumb_num - 1")
                        .eq("id", commentId)
                        .gt("thumb_num", 0)
                        .update();
                return result ? -1 : 0;
            } else {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR);
            }
        } else {
            // 未点赞
            result = this.save(postCommentThumb);
            if (result) {
                // 点赞数 + 1
                result = postCommentService.update()
                        .setSql("thumb_num = thumb_num + 1")
                        .eq("id", commentId)
                        .update();
                return result ? 1 : 0;
            } else {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR);
            }
        }
    }
}