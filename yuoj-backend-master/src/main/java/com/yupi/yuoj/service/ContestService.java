package com.yupi.yuoj.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yupi.yuoj.model.dto.contest.ContestQueryRequest;
import com.yupi.yuoj.model.entity.Contest;
import com.yupi.yuoj.model.vo.ContestVO;
import javax.servlet.http.HttpServletRequest;

/**
 * Contest service
 */
public interface ContestService extends IService<Contest> {

    QueryWrapper<Contest> getQueryWrapper(ContestQueryRequest request);

    ContestVO getVO(Contest contest, HttpServletRequest request);

    Page<ContestVO> getVOPage(Page<Contest> page, HttpServletRequest request);
}
