package com.yupi.yuoj.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yupi.yuoj.mapper.ContestSignupMapper;
import com.yupi.yuoj.model.dto.contest.ContestSignupQueryRequest;
import com.yupi.yuoj.model.entity.ContestSignup;
import com.yupi.yuoj.service.ContestSignupService;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Service;

/**
 * Contest signup service
 */
@Service
public class ContestSignupServiceImpl extends ServiceImpl<ContestSignupMapper, ContestSignup>
        implements ContestSignupService {

    @Override
    public QueryWrapper<ContestSignup> getQueryWrapper(ContestSignupQueryRequest request) {
        QueryWrapper<ContestSignup> queryWrapper = new QueryWrapper<>();
        if (request == null) {
            return queryWrapper;
        }
        Long contestId = request.getContestId();
        Long userId = request.getUserId();
        Integer status = request.getStatus();
        queryWrapper.eq(ObjectUtils.isNotEmpty(contestId), "contest_id", contestId);
        queryWrapper.eq(ObjectUtils.isNotEmpty(userId), "user_id", userId);
        queryWrapper.eq(ObjectUtils.isNotEmpty(status), "status", status);
        return queryWrapper;
    }
}
