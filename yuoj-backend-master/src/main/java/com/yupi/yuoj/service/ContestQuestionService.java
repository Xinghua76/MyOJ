package com.yupi.yuoj.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yupi.yuoj.model.dto.contest.ContestQuestionQueryRequest;
import com.yupi.yuoj.model.entity.ContestQuestion;
import com.yupi.yuoj.model.vo.ContestQuestionVO;

import javax.servlet.http.HttpServletRequest;

/**
 * Contest question service
 */
public interface ContestQuestionService extends IService<ContestQuestion> {

    QueryWrapper<ContestQuestion> getQueryWrapper(ContestQuestionQueryRequest request);

    Page<ContestQuestionVO> getContestQuestionVOPage(Page<ContestQuestion> page, HttpServletRequest request);
}
