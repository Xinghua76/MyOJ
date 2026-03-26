package com.yupi.yuoj.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yupi.yuoj.annotation.AuthCheck;
import com.yupi.yuoj.common.BaseResponse;
import com.yupi.yuoj.common.DeleteRequest;
import com.yupi.yuoj.common.ErrorCode;
import com.yupi.yuoj.common.ResultUtils;
import com.yupi.yuoj.constant.UserConstant;
import com.yupi.yuoj.exception.BusinessException;
import com.yupi.yuoj.exception.ThrowUtils;
import com.yupi.yuoj.model.dto.solutioncomment.SolutionCommentAddRequest;
import com.yupi.yuoj.model.dto.solutioncomment.SolutionCommentQueryRequest;
import com.yupi.yuoj.model.dto.solutioncomment.SolutionCommentUpdateRequest;
import com.yupi.yuoj.model.entity.SolutionComment;
import com.yupi.yuoj.model.entity.Post;
import com.yupi.yuoj.model.entity.User;
import com.yupi.yuoj.model.vo.SolutionCommentVO;
import com.yupi.yuoj.service.PostService;
import com.yupi.yuoj.service.SolutionCommentService;
import com.yupi.yuoj.service.UserService;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Solution comment endpoints
 */
@RestController
@RequestMapping("/solution_comment")
public class SolutionCommentController {

    @Resource
    private SolutionCommentService solutionCommentService;

    @Resource
    private UserService userService;

    @Resource
    private PostService postService;

    @PostMapping("/add")
    public BaseResponse<Long> add(@RequestBody SolutionCommentAddRequest request, HttpServletRequest httpRequest) {
        if (request == null || request.getSolutionId() == null || request.getSolutionId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        if (StringUtils.isBlank(request.getContent())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "content required");
        }
        User loginUser = userService.getLoginUser(httpRequest);
        Post solution = postService.getById(request.getSolutionId());
        ThrowUtils.throwIf(solution == null, ErrorCode.NOT_FOUND_ERROR);
        // Post no longer has status field, assuming always visible or handled elsewhere
        if (request.getParentId() != null && request.getParentId() > 0) {
            SolutionComment parent = solutionCommentService.getById(request.getParentId());
            ThrowUtils.throwIf(parent == null, ErrorCode.NOT_FOUND_ERROR);
            if (!parent.getSolutionId().equals(request.getSolutionId())) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "parent not in same solution");
            }
        }
        SolutionComment comment = new SolutionComment();
        BeanUtils.copyProperties(request, comment);
        comment.setUserId(loginUser.getId());
        comment.setLikeNum(0);
        if (comment.getStatus() == null) {
            comment.setStatus(1);
        }
        boolean result = solutionCommentService.save(comment);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        // update comment count on solution (post no longer has commentNum)
        // solution.setCommentNum(commentNum + 1);
        // postService.updateById(solution);
        return ResultUtils.success(comment.getId());
    }

    @PostMapping("/delete")
    public BaseResponse<Boolean> delete(@RequestBody DeleteRequest deleteRequest, HttpServletRequest httpRequest) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(httpRequest);
        SolutionComment old = solutionCommentService.getById(deleteRequest.getId());
        ThrowUtils.throwIf(old == null, ErrorCode.NOT_FOUND_ERROR);
        if (!old.getUserId().equals(loginUser.getId()) && !userService.isAdmin(loginUser)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        boolean result = solutionCommentService.removeById(deleteRequest.getId());
        if (result) {
            Post solution = postService.getById(old.getSolutionId());
            if (solution != null) {
                // update comment count on solution (post no longer has commentNum)
                // solution.setCommentNum(Math.max(0, commentNum - 1));
                // postService.updateById(solution);
            }
        }
        return ResultUtils.success(result);
    }

    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> update(@RequestBody SolutionCommentUpdateRequest request) {
        if (request == null || request.getId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        if (StringUtils.isBlank(request.getContent()) && request.getStatus() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        SolutionComment comment = new SolutionComment();
        BeanUtils.copyProperties(request, comment);
        boolean result = solutionCommentService.updateById(comment);
        return ResultUtils.success(result);
    }

    @PostMapping("/list/page/vo")
    public BaseResponse<Page<SolutionCommentVO>> listPage(@RequestBody SolutionCommentQueryRequest request,
            HttpServletRequest httpRequest) {
        long current = request.getCurrent();
        long size = request.getPageSize();
        Page<SolutionComment> page = solutionCommentService.page(new Page<>(current, size),
                solutionCommentService.getQueryWrapper(request));
        return ResultUtils.success(solutionCommentService.getVOPage(page, httpRequest));
    }

    @PostMapping("/my/list/page/vo")
    public BaseResponse<Page<SolutionCommentVO>> listMyPage(@RequestBody SolutionCommentQueryRequest request,
            HttpServletRequest httpRequest) {
        if (request == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(httpRequest);
        request.setUserId(loginUser.getId());
        long current = request.getCurrent();
        long size = request.getPageSize();
        Page<SolutionComment> page = solutionCommentService.page(new Page<>(current, size),
                solutionCommentService.getQueryWrapper(request));
        return ResultUtils.success(solutionCommentService.getVOPage(page, httpRequest));
    }
}
