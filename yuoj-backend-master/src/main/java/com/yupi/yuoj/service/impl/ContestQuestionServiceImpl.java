package com.yupi.yuoj.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yupi.yuoj.mapper.ContestQuestionMapper;
import com.yupi.yuoj.model.dto.contest.ContestQuestionQueryRequest;
import com.yupi.yuoj.model.entity.ContestQuestion;
import com.yupi.yuoj.model.entity.Question;
import com.yupi.yuoj.model.vo.ContestQuestionVO;
import com.yupi.yuoj.service.ContestQuestionService;
import com.yupi.yuoj.service.QuestionService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Contest question service
 */
@Service
public class ContestQuestionServiceImpl extends ServiceImpl<ContestQuestionMapper, ContestQuestion>
        implements ContestQuestionService {

    @Resource
    private QuestionService questionService;

    @Override
    public QueryWrapper<ContestQuestion> getQueryWrapper(ContestQuestionQueryRequest request) {
        QueryWrapper<ContestQuestion> queryWrapper = new QueryWrapper<>();
        if (request == null) {
            return queryWrapper;
        }
        Long contestId = request.getContestId();
        Long questionId = request.getQuestionId();
        queryWrapper.eq(ObjectUtils.isNotEmpty(contestId), "contest_id", contestId);
        queryWrapper.eq(ObjectUtils.isNotEmpty(questionId), "question_id", questionId);
        return queryWrapper;
    }

    @Override
    public Page<ContestQuestionVO> getContestQuestionVOPage(Page<ContestQuestion> page, HttpServletRequest request) {
        List<ContestQuestion> contestQuestionList = page.getRecords();
        Page<ContestQuestionVO> contestQuestionVOPage = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        if (CollectionUtils.isEmpty(contestQuestionList)) {
            return contestQuestionVOPage;
        }
        // 1. Get Questions
        Set<Long> questionIds = contestQuestionList.stream().map(ContestQuestion::getQuestionId).collect(Collectors.toSet());
        Map<Long, Question> questionMap = questionService.listByIds(questionIds).stream()
                .collect(Collectors.toMap(Question::getId, q -> q));
        
        // 2. Fill VO
        List<ContestQuestionVO> contestQuestionVOList = contestQuestionList.stream().map(cq -> {
            ContestQuestionVO vo = ContestQuestionVO.objToVo(cq);
            if (questionMap.containsKey(cq.getQuestionId())) {
                vo.setQuestionVO(questionService.getQuestionVO(questionMap.get(cq.getQuestionId()), request));
            }
            return vo;
        }).collect(Collectors.toList());
        
        contestQuestionVOPage.setRecords(contestQuestionVOList);
        return contestQuestionVOPage;
    }
}
