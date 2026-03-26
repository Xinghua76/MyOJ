package com.yupi.yuoj.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yupi.yuoj.model.dto.solutioncomment.SolutionCommentQueryRequest;
import com.yupi.yuoj.model.entity.SolutionComment;
import com.yupi.yuoj.model.vo.SolutionCommentVO;
import javax.servlet.http.HttpServletRequest;

/**
 * Solution comment service
 */
public interface SolutionCommentService extends IService<SolutionComment> {

    QueryWrapper<SolutionComment> getQueryWrapper(SolutionCommentQueryRequest request);

    SolutionCommentVO getVO(SolutionComment comment, HttpServletRequest request);

    Page<SolutionCommentVO> getVOPage(Page<SolutionComment> page, HttpServletRequest request);
}
