package com.yupi.yuoj.controller;

import com.yupi.yuoj.common.BaseResponse;
import com.yupi.yuoj.common.ErrorCode;
import com.yupi.yuoj.common.ResultUtils;
import com.yupi.yuoj.exception.BusinessException;
import com.yupi.yuoj.model.dto.solutioncommentthumb.SolutionCommentThumbAddRequest;
import com.yupi.yuoj.model.entity.User;
import com.yupi.yuoj.service.SolutionCommentThumbService;
import com.yupi.yuoj.service.UserService;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 题解评论点赞接口
 */
@RestController
@RequestMapping("/solution_comment_thumb")
@Slf4j
public class SolutionCommentThumbController {

    @Resource
    private SolutionCommentThumbService solutionCommentThumbService;

    @Resource
    private UserService userService;

    /**
     * 点赞 / 取消点赞
     *
     * @param solutionCommentThumbAddRequest
     * @param request
     * @return resultNum 本次点赞变化数
     */
    @PostMapping("/")
    public BaseResponse<Integer> doThumb(@RequestBody SolutionCommentThumbAddRequest solutionCommentThumbAddRequest,
            HttpServletRequest request) {
        if (solutionCommentThumbAddRequest == null || solutionCommentThumbAddRequest.getCommentId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 登录才能点赞
        final User loginUser = userService.getLoginUser(request);
        long commentId = solutionCommentThumbAddRequest.getCommentId();
        int result = solutionCommentThumbService.doSolutionCommentThumb(commentId, loginUser);
        return ResultUtils.success(result);
    }

}
