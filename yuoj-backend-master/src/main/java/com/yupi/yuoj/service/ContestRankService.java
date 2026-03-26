package com.yupi.yuoj.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yupi.yuoj.model.dto.contest.ContestRankQueryRequest;
import com.yupi.yuoj.model.entity.ContestRank;
import com.yupi.yuoj.model.vo.ContestRankVO;

/**
 * Contest rank service
 */
public interface ContestRankService extends IService<ContestRank> {

    QueryWrapper<ContestRank> getQueryWrapper(ContestRankQueryRequest request);

    Page<ContestRankVO> listContestRankVOByPage(ContestRankQueryRequest request);
}
