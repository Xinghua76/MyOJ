package com.yupi.yuoj.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yupi.yuoj.common.BaseResponse;
import com.yupi.yuoj.common.DeleteRequest;
import com.yupi.yuoj.common.ErrorCode;
import com.yupi.yuoj.common.ResultUtils;
import com.yupi.yuoj.exception.BusinessException;
import com.yupi.yuoj.exception.ThrowUtils;
import com.yupi.yuoj.model.dto.contest.ContestSignupQueryRequest;
import com.yupi.yuoj.model.dto.contest.ContestSignupRequest;
import com.yupi.yuoj.model.entity.Contest;
import com.yupi.yuoj.model.entity.ContestSignup;
import com.yupi.yuoj.model.entity.User;
import com.yupi.yuoj.service.ContestService;
import com.yupi.yuoj.service.ContestSignupService;
import com.yupi.yuoj.service.UserService;
import java.util.Date;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Contest signup endpoints
 */
@RestController
@RequestMapping("/contest_signup")
public class ContestSignupController {

    @Resource
    private ContestSignupService contestSignupService;

    @Resource
    private UserService userService;

    @Resource
    private ContestService contestService;

    @PostMapping("/do")
    public BaseResponse<Boolean> signup(@RequestBody ContestSignupRequest request, HttpServletRequest httpRequest) {
        if (request == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Long contestId = request.getContestId();
        if (contestId == null || contestId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "Invalid contest id");
        }
        Contest contest = contestService.getById(contestId);
        ThrowUtils.throwIf(contest == null, ErrorCode.NOT_FOUND_ERROR, "Contest not found");
        Date now = new Date();
        if (contest.getEndTime() != null && now.after(contest.getEndTime())) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "contest ended");
        }
        if (contest.getStatus() != null && contest.getStatus() == 2) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "contest ended");
        }
        // 比赛开始后，不允许报名
        if (contest.getStartTime() != null && now.after(contest.getStartTime())) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "contest started, cannot signup");
        }
        User loginUser = userService.getLoginUser(httpRequest);
        QueryWrapper<ContestSignup> wrapper = new QueryWrapper<>();
        wrapper.eq("contest_id", request.getContestId());
        wrapper.eq("user_id", loginUser.getId());
        ContestSignup existing = contestSignupService.getOne(wrapper);
        if (existing != null) {
            if (existing.getStatus() != null && existing.getStatus() == 1) {
                return ResultUtils.success(true);
            }
            existing.setStatus(1);
            boolean updated = contestSignupService.updateById(existing);
            return ResultUtils.success(updated);
        }
        ContestSignup signup = new ContestSignup();
        signup.setContestId(request.getContestId());
        signup.setUserId(loginUser.getId());
        signup.setStatus(1);
        boolean result = contestSignupService.save(signup);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    @PostMapping("/cancel")
    public BaseResponse<Boolean> cancel(@RequestBody DeleteRequest request, HttpServletRequest httpRequest) {
        if (request == null || request.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(httpRequest);
        ContestSignup signup = contestSignupService.getById(request.getId());
        ThrowUtils.throwIf(signup == null, ErrorCode.NOT_FOUND_ERROR);
        if (!signup.getUserId().equals(loginUser.getId()) && !userService.isAdmin(loginUser)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }

        // Check if contest started
        Contest contest = contestService.getById(signup.getContestId());
        if (contest != null) {
            Date now = new Date();
            if (contest.getStartTime() != null && now.after(contest.getStartTime())) {
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "contest started, cannot cancel signup");
            }
        }

        signup.setStatus(0);
        boolean result = contestSignupService.updateById(signup);
        return ResultUtils.success(result);
    }

    @PostMapping("/list/page")
    public BaseResponse<Page<ContestSignup>> listContestSignupByPage(@RequestBody ContestSignupQueryRequest request) {
        long current = request.getCurrent();
        long size = request.getPageSize();
        Page<ContestSignup> page = contestSignupService.page(new Page<>(current, size),
                contestSignupService.getQueryWrapper(request));
        return ResultUtils.success(page);
    }
}
