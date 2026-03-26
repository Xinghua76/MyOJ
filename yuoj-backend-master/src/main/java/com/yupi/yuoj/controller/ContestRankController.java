package com.yupi.yuoj.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yupi.yuoj.common.BaseResponse;
import com.yupi.yuoj.common.ErrorCode;
import com.yupi.yuoj.common.ResultUtils;
import com.yupi.yuoj.exception.BusinessException;
import com.yupi.yuoj.model.dto.contest.ContestRankQueryRequest;
import com.yupi.yuoj.model.entity.ContestRank;
import com.yupi.yuoj.model.vo.ContestRankVO;
import com.yupi.yuoj.service.ContestRankService;
import javax.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Contest rank endpoints
 */
@RestController
@RequestMapping("/contest_rank")
public class ContestRankController {

    @Resource
    private ContestRankService contestRankService;

    @PostMapping("/list/page")
    public BaseResponse<Page<ContestRankVO>> listContestRankByPage(@RequestBody ContestRankQueryRequest request) {
        if (request == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Page<ContestRankVO> page = contestRankService.listContestRankVOByPage(request);
        return ResultUtils.success(page);
    }
}
