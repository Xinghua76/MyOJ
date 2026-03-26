package com.yupi.yuoj.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yupi.yuoj.common.ErrorCode;
import com.yupi.yuoj.constant.CommonConstant;
import com.yupi.yuoj.exception.BusinessException;
import com.yupi.yuoj.exception.ThrowUtils;
import com.yupi.yuoj.mapper.QuestionMapper;
import com.yupi.yuoj.model.dto.question.QuestionQueryRequest;
import com.yupi.yuoj.model.entity.Question;
import com.yupi.yuoj.model.entity.User;
import com.yupi.yuoj.model.vo.QuestionVO;
import com.yupi.yuoj.model.vo.UserVO;
import com.yupi.yuoj.service.QuestionService;
import com.yupi.yuoj.service.UserService;
import com.yupi.yuoj.utils.SqlUtils;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

/**
 * Question service
 */
@Service
public class QuestionServiceImpl extends ServiceImpl<QuestionMapper, Question> implements QuestionService {

    @Resource
    private UserService userService;

    @Override
    public void validQuestion(Question question, boolean add) {
        if (question == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String title = question.getTitle();
        String content = question.getContent();
        String tags = question.getTags();
        String answer = question.getAnswer();
        String judgeCase = question.getJudgeCase();
        String judgeConfig = question.getJudgeConfig();
        Integer difficulty = question.getDifficulty();
        if (add) {
            ThrowUtils.throwIf(StringUtils.isAnyBlank(title, content), ErrorCode.PARAMS_ERROR);
        }
        if (StringUtils.isNotBlank(title) && title.length() > 80) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "title too long");
        }
        if (StringUtils.isNotBlank(content) && content.length() > 8192) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "content too long");
        }
        if (StringUtils.isNotBlank(answer) && answer.length() > 8192) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "answer too long");
        }
        if (StringUtils.isNotBlank(judgeCase) && judgeCase.length() > 8192) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "judgeCase too long");
        }
        if (StringUtils.isNotBlank(judgeConfig) && judgeConfig.length() > 8192) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "judgeConfig too long");
        }
        if (difficulty != null && (difficulty < 1 || difficulty > 3)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "difficulty invalid");
        }
        if (tags != null && tags.length() > 4096) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "tags too long");
        }
    }

    @Override
    public QueryWrapper<Question> getQueryWrapper(QuestionQueryRequest questionQueryRequest) {
        QueryWrapper<Question> queryWrapper = new QueryWrapper<>();
        if (questionQueryRequest == null) {
            return queryWrapper;
        }
        Long id = questionQueryRequest.getId();
        String title = questionQueryRequest.getTitle();
        String content = questionQueryRequest.getContent();
        List<String> tags = questionQueryRequest.getTags();
        String answer = questionQueryRequest.getAnswer();
        Long userId = questionQueryRequest.getUserId();
        Integer difficulty = questionQueryRequest.getDifficulty();
        Integer status = questionQueryRequest.getStatus();
        String sortField = questionQueryRequest.getSortField();
        String sortOrder = questionQueryRequest.getSortOrder();
        String keyword = questionQueryRequest.getKeyword();

        // keyword search
        if (StringUtils.isNotBlank(keyword)) {
            queryWrapper.and(qw -> qw.like("title", keyword).or().like("content", keyword).or().like("tags", "\"" + keyword + "\""));
        }

        queryWrapper.like(StringUtils.isNotBlank(title), "title", title);
        queryWrapper.like(StringUtils.isNotBlank(content), "content", content);
        queryWrapper.like(StringUtils.isNotBlank(answer), "answer", answer);
        if (CollectionUtils.isNotEmpty(tags)) {
            for (String tag : tags) {
                queryWrapper.like("tags", "\"" + tag + "\"");
            }
        }
        queryWrapper.eq(ObjectUtils.isNotEmpty(id), "id", id);
        queryWrapper.eq(ObjectUtils.isNotEmpty(userId), "user_id", userId);
        queryWrapper.eq(ObjectUtils.isNotEmpty(difficulty), "difficulty", difficulty);
        queryWrapper.eq(ObjectUtils.isNotEmpty(status), "status", status);
        queryWrapper.eq("is_delete", 0);
        queryWrapper.orderBy(SqlUtils.validSortField(sortField), sortOrder.equals(CommonConstant.SORT_ORDER_ASC),
                sortField);
        return queryWrapper;
    }

    @Override
    public QuestionVO getQuestionVO(Question question, HttpServletRequest request) {
        QuestionVO questionVO = QuestionVO.objToVo(question);
        Long userId = question.getUserId();
        User user = null;
        if (userId != null && userId > 0) {
            user = userService.getById(userId);
        }
        UserVO userVO = userService.getUserVO(user);
        questionVO.setUserVO(userVO);
        return questionVO;
    }

    @Override
    public Page<QuestionVO> getQuestionVOPage(Page<Question> questionPage, HttpServletRequest request) {
        List<Question> questionList = questionPage.getRecords();
        Page<QuestionVO> questionVOPage = new Page<>(questionPage.getCurrent(), questionPage.getSize(), questionPage.getTotal());
        if (CollectionUtils.isEmpty(questionList)) {
            return questionVOPage;
        }
        Set<Long> userIdSet = questionList.stream().map(Question::getUserId).collect(Collectors.toSet());
        Map<Long, List<User>> userIdUserListMap = userService.listByIds(userIdSet).stream()
                .collect(Collectors.groupingBy(User::getId));
        List<QuestionVO> questionVOList = questionList.stream().map(question -> {
            QuestionVO questionVO = QuestionVO.objToVo(question);
            Long userId = question.getUserId();
            User user = null;
            if (userIdUserListMap.containsKey(userId)) {
                user = userIdUserListMap.get(userId).get(0);
            }
            questionVO.setUserVO(userService.getUserVO(user));
            return questionVO;
        }).collect(Collectors.toList());
        questionVOPage.setRecords(questionVOList);
        return questionVOPage;
    }
}
