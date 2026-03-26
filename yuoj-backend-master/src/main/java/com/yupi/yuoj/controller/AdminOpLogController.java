package com.yupi.yuoj.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.yupi.yuoj.annotation.AuthCheck;
import com.yupi.yuoj.common.BaseResponse;
import com.yupi.yuoj.common.ResultUtils;
import com.yupi.yuoj.constant.UserConstant;
import com.yupi.yuoj.model.entity.AdminOpLog;
import com.yupi.yuoj.service.AdminOpLogService;
import com.yupi.yuoj.common.PageRequest;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;

@RestController
@RequestMapping("/admin_op_log")
public class AdminOpLogController {

    @Resource
    private AdminOpLogService adminOpLogService;

    @PostMapping("/list/page")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<AdminOpLog>> listAdminOpLogByPage(@RequestBody PageRequest pageRequest) {
        long current = pageRequest.getCurrent();
        long size = pageRequest.getPageSize();
        QueryWrapper<AdminOpLog> queryWrapper = new QueryWrapper<>();
        queryWrapper.orderByDesc("create_time");
        Page<AdminOpLog> page = adminOpLogService.page(new Page<>(current, size), queryWrapper);
        return ResultUtils.success(page);
    }
}
