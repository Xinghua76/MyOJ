package com.yupi.yuoj.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yupi.yuoj.model.dto.postcomment.PostCommentQueryRequest;
import com.yupi.yuoj.model.entity.PostComment;
import com.yupi.yuoj.model.vo.PostCommentVO;
import javax.servlet.http.HttpServletRequest;

/**
 * 帖子评论服务
 */
public interface PostCommentService extends IService<PostComment> {

    QueryWrapper<PostComment> getQueryWrapper(PostCommentQueryRequest request);

    PostCommentVO getPostCommentVO(PostComment postComment, HttpServletRequest request);

    Page<PostCommentVO> getPostCommentVOPage(Page<PostComment> page, HttpServletRequest request);
}