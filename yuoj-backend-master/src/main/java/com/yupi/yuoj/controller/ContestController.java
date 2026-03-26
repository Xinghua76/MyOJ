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
import com.yupi.yuoj.model.dto.contest.ContestAddRequest;
import com.yupi.yuoj.model.dto.contest.ContestQueryRequest;
import com.yupi.yuoj.model.dto.contest.ContestUpdateRequest;
import com.yupi.yuoj.model.entity.Contest;
import com.yupi.yuoj.model.entity.User;
import com.yupi.yuoj.model.vo.ContestVO;
import com.yupi.yuoj.service.ContestService;
import com.yupi.yuoj.service.UserService;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Contest endpoints
 */
@RestController
@RequestMapping("/contest")
public class ContestController {

    @Resource
    private ContestService contestService;

    @Resource
    private UserService userService;

    @PostMapping("/add")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Long> add(@RequestBody ContestAddRequest request, HttpServletRequest httpRequest) {
        if (request == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        if (request.getStartTime() == null || request.getEndTime() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "开始时间或结束时间不能为空");
        }
        User loginUser = userService.getLoginUser(httpRequest);
        Contest contest = new Contest();
        BeanUtils.copyProperties(request, contest);
        contest.setCreatorId(loginUser.getId());
        if (contest.getStatus() == null) {
            contest.setStatus(0);
        }
        boolean result = contestService.save(contest);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(contest.getId());
    }

    @PostMapping("/delete")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> delete(@RequestBody DeleteRequest deleteRequest) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean result = contestService.removeById(deleteRequest.getId());
        return ResultUtils.success(result);
    }

    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> update(@RequestBody ContestUpdateRequest request) {
        if (request == null || request.getId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Contest contest = new Contest();
        BeanUtils.copyProperties(request, contest);
        boolean result = contestService.updateById(contest);
        return ResultUtils.success(result);
    }

    @GetMapping("/get/vo")
    public BaseResponse<ContestVO> getVO(String id, HttpServletRequest httpRequest) {
        if (id == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        long contestId = Long.parseLong(id);
        if (contestId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Contest contest = contestService.getById(contestId);
        ThrowUtils.throwIf(contest == null, ErrorCode.NOT_FOUND_ERROR);
        return ResultUtils.success(contestService.getVO(contest, httpRequest));
    }

    @PostMapping("/list/page/vo")
    public BaseResponse<Page<ContestVO>> listContestVOByPage(@RequestBody ContestQueryRequest request,
            HttpServletRequest httpRequest) {
        long current = request.getCurrent();
        long size = request.getPageSize();
        Page<Contest> page = contestService.page(new Page<>(current, size), contestService.getQueryWrapper(request));
        return ResultUtils.success(contestService.getVOPage(page, httpRequest));
    }

    @PostMapping("/list/page")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<Contest>> listContestByPage(@RequestBody ContestQueryRequest request) {
        long current = request.getCurrent();
        long size = request.getPageSize();
        Page<Contest> page = contestService.page(new Page<>(current, size), contestService.getQueryWrapper(request));
        return ResultUtils.success(page);
    }
}
