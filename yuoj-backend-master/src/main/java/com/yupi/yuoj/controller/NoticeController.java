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
import com.yupi.yuoj.model.dto.notice.NoticeAddRequest;
import com.yupi.yuoj.model.dto.notice.NoticeQueryRequest;
import com.yupi.yuoj.model.dto.notice.NoticeUpdateRequest;
import com.yupi.yuoj.model.entity.Notice;
import com.yupi.yuoj.model.entity.User;
import com.yupi.yuoj.model.vo.NoticeVO;
import com.yupi.yuoj.service.NoticeService;
import com.yupi.yuoj.service.UserService;
import java.util.Date;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Notice endpoints
 */
@RestController
@RequestMapping("/notice")
public class NoticeController {

    @Resource
    private NoticeService noticeService;

    @Resource
    private UserService userService;

    @PostMapping("/add")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Long> add(@RequestBody NoticeAddRequest request, HttpServletRequest httpRequest) {
        if (request == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(httpRequest);
        Notice notice = new Notice();
        BeanUtils.copyProperties(request, notice);
        notice.setPublisherId(loginUser.getId());
        if (notice.getStatus() == null) {
            notice.setStatus(0);
        }
        if (notice.getStatus() < 0 || notice.getStatus() > 2) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "invalid status");
        }
        if (notice.getPublishTime() == null) {
            notice.setPublishTime(new Date());
        }
        boolean result = noticeService.save(notice);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(notice.getId());
    }

    @PostMapping("/delete")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> delete(@RequestBody DeleteRequest request) {
        if (request == null || request.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean result = noticeService.removeById(request.getId());
        return ResultUtils.success(result);
    }

    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> update(@RequestBody NoticeUpdateRequest request) {
        if (request == null || request.getId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        if (request.getStatus() != null && (request.getStatus() < 0 || request.getStatus() > 2)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "invalid status");
        }
        Notice notice = new Notice();
        BeanUtils.copyProperties(request, notice);
        boolean result = noticeService.updateById(notice);
        return ResultUtils.success(result);
    }

    @GetMapping("/get/vo")
    public BaseResponse<NoticeVO> getVO(long id, HttpServletRequest httpRequest) {
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Notice notice = noticeService.getById(id);
        ThrowUtils.throwIf(notice == null, ErrorCode.NOT_FOUND_ERROR);
        return ResultUtils.success(noticeService.getVO(notice, httpRequest));
    }

    @PostMapping("/list/page/vo")
    public BaseResponse<Page<NoticeVO>> listPage(@RequestBody NoticeQueryRequest request, HttpServletRequest httpRequest) {
        long current = request.getCurrent();
        long size = request.getPageSize();
        Page<Notice> page = noticeService.page(new Page<>(current, size), noticeService.getQueryWrapper(request));
        return ResultUtils.success(noticeService.getVOPage(page, httpRequest));
    }
}
