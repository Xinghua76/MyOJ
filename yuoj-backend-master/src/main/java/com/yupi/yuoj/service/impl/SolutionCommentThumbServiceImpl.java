package com.yupi.yuoj.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yupi.yuoj.common.ErrorCode;
import com.yupi.yuoj.exception.BusinessException;
import com.yupi.yuoj.mapper.SolutionCommentThumbMapper;
import com.yupi.yuoj.model.entity.SolutionComment;
import com.yupi.yuoj.model.entity.SolutionCommentThumb;
import com.yupi.yuoj.model.entity.User;
import com.yupi.yuoj.service.SolutionCommentService;
import com.yupi.yuoj.service.SolutionCommentThumbService;
import javax.annotation.Resource;
import org.springframework.aop.framework.AopContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 题解评论点赞服务实现
 */
@Service
public class SolutionCommentThumbServiceImpl extends ServiceImpl<SolutionCommentThumbMapper, SolutionCommentThumb>
        implements SolutionCommentThumbService {

    @Resource
    private SolutionCommentService solutionCommentService;

    /**
     * 点赞
     *
     * @param commentId
     * @param loginUser
     * @return
     */
    @Override
    public int doSolutionCommentThumb(long commentId, User loginUser) {
        // 判断实体是否存在，根据类别获取实体
        SolutionComment comment = solutionCommentService.getById(commentId);
        if (comment == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        // 是否已点赞
        long userId = loginUser.getId();
        // 每个用户串行点赞
        // 锁必须要包裹住事务方法
        SolutionCommentThumbService solutionCommentThumbService = (SolutionCommentThumbService) AopContext.currentProxy();
        synchronized (String.valueOf(userId).intern()) {
            return solutionCommentThumbService.doSolutionCommentThumbInner(userId, commentId);
        }
    }

    /**
     * 封装了事务的方法
     *
     * @param userId
     * @param commentId
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int doSolutionCommentThumbInner(long userId, long commentId) {
        SolutionCommentThumb thumb = new SolutionCommentThumb();
        thumb.setUserId(userId);
        thumb.setCommentId(commentId);
        QueryWrapper<SolutionCommentThumb> thumbQueryWrapper = new QueryWrapper<>(thumb);
        SolutionCommentThumb oldThumb = this.getOne(thumbQueryWrapper);
        boolean result;
        // 已点赞
        if (oldThumb != null) {
            result = this.remove(thumbQueryWrapper);
            if (result) {
                // 点赞数 - 1
                result = solutionCommentService.update().setSql("like_num = like_num - 1").eq("id", commentId).gt("like_num", 0)
                        .update();
                return result ? -1 : 0;
            } else {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR);
            }
        } else {
            // 未点赞
            result = this.save(thumb);
            if (result) {
                // 点赞数 + 1
                result = solutionCommentService.update().setSql("like_num = like_num + 1").eq("id", commentId).update();
                return result ? 1 : 0;
            } else {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR);
            }
        }
    }
}
