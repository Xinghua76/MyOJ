package com.yupi.yuoj.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yupi.yuoj.model.dto.contest.ContestSignupQueryRequest;
import com.yupi.yuoj.model.entity.ContestSignup;

/**
 * Contest signup service
 */
public interface ContestSignupService extends IService<ContestSignup> {

    QueryWrapper<ContestSignup> getQueryWrapper(ContestSignupQueryRequest request);
}
