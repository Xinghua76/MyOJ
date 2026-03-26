package com.yupi.yuoj.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yupi.yuoj.common.ErrorCode;
import com.yupi.yuoj.constant.CommonConstant;
import com.yupi.yuoj.exception.BusinessException;
import com.yupi.yuoj.judge.JudgeService;
import com.yupi.yuoj.judge.queue.producer.JudgeTaskProducer;
import com.yupi.yuoj.mapper.QuestionSubmitMapper;
import com.yupi.yuoj.model.dto.questionsubmit.QuestionSubmitAddRequest;
import com.yupi.yuoj.model.dto.questionsubmit.QuestionSubmitQueryRequest;
import com.yupi.yuoj.model.entity.*;
import com.yupi.yuoj.model.enums.QuestionSubmitLanguageEnum;
import com.yupi.yuoj.model.enums.QuestionSubmitStatusEnum;
import com.yupi.yuoj.model.vo.QuestionSubmitVO;
import com.yupi.yuoj.service.ContestQuestionService;
import com.yupi.yuoj.service.ContestService;
import com.yupi.yuoj.service.ContestSignupService;
import com.yupi.yuoj.service.QuestionService;
import com.yupi.yuoj.service.QuestionSubmitService;
import com.yupi.yuoj.service.UserService;
import com.yupi.yuoj.utils.SqlUtils;
import cn.hutool.json.JSONUtil;
import com.yupi.yuoj.judge.codesandbox.model.JudgeInfo;
import com.yupi.yuoj.model.enums.JudgeInfoMessageEnum;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.Resource;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

/**
 * Question submit service
 */
@Service
public class QuestionSubmitServiceImpl extends ServiceImpl<QuestionSubmitMapper, QuestionSubmit>
        implements QuestionSubmitService {

    @Resource
    private QuestionService questionService;

    @Resource
    private UserService userService;

    @Resource
    @Lazy
    private JudgeService judgeService;

    @Resource
    private JudgeTaskProducer judgeTaskProducer;

    @Resource
    private ContestService contestService;

    @Resource
    private ContestSignupService contestSignupService;

    @Resource
    private ContestQuestionService contestQuestionService;

    @Override
    public long doQuestionSubmit(QuestionSubmitAddRequest questionSubmitAddRequest, User loginUser) {
        String language = questionSubmitAddRequest.getLanguage();
        QuestionSubmitLanguageEnum languageEnum = QuestionSubmitLanguageEnum.getEnumByValue(language);
        if (languageEnum == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "language invalid");
        }
        long questionId = questionSubmitAddRequest.getQuestionId();
        Question question = questionService.getById(questionId);
        if (question == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        long userId = loginUser.getId();
        Long contestId = questionSubmitAddRequest.getContestId();
        if (contestId != null && contestId > 0) {
            // Check contest
            Contest contest = contestService.getById(contestId);
            if (contest == null) {
                throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "contest not found");
            }
            Date now = new Date();
            if (contest.getStartTime() != null && now.before(contest.getStartTime())) {
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "contest not started");
            }
            if (contest.getEndTime() != null && now.after(contest.getEndTime())) {
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "contest ended");
            }
            // Check signup
            QueryWrapper<ContestSignup> signupQuery = new QueryWrapper<>();
            signupQuery.eq("contest_id", contestId);
            signupQuery.eq("user_id", userId);
            signupQuery.eq("status", 1);
            long count = contestSignupService.count(signupQuery);
            if (count == 0) {
                throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "not signed up");
            }
            // Check if question belongs to contest
            QueryWrapper<ContestQuestion> cqQuery = new QueryWrapper<>();
            cqQuery.eq("contest_id", contestId);
            cqQuery.eq("question_id", questionId);
            long cqCount = contestQuestionService.count(cqQuery);
            if (cqCount == 0) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "question not in contest");
            }
        }

        QuestionSubmit questionSubmit = new QuestionSubmit();
        questionSubmit.setUserId(userId);
        questionSubmit.setQuestionId(questionId);
        questionSubmit.setContestId(contestId);
        questionSubmit.setCode(questionSubmitAddRequest.getCode());
        questionSubmit.setLanguage(language);
        questionSubmit.setStatus(QuestionSubmitStatusEnum.WAITING.getValue());
        questionSubmit.setJudgeInfo("{}");
        boolean save = this.save(questionSubmit);
        if (!save) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "insert failed");
        }
        // 提交成功后，立即更新题目的提交数（submitNum + 1）
        Question updateQuestion = new Question();
        updateQuestion.setId(questionId);
        updateQuestion.setSubmitNum(question.getSubmitNum() + 1);
        boolean updateQuestionResult = questionService.updateById(updateQuestion);
        if (!updateQuestionResult) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "更新题目提交数失败");
        }
        Long questionSubmitId = questionSubmit.getId();
        try {
            judgeTaskProducer.sendJudgeTask(questionSubmitId);
        } catch (Exception e) {
            JudgeInfo judgeInfo = new JudgeInfo();
            judgeInfo.setMessage(JudgeInfoMessageEnum.SYSTEM_ERROR.getValue());
            QuestionSubmit updateSubmit = new QuestionSubmit();
            updateSubmit.setId(questionSubmitId);
            updateSubmit.setStatus(QuestionSubmitStatusEnum.FAILED.getValue());
            updateSubmit.setJudgeInfo(JSONUtil.toJsonStr(judgeInfo));
            this.updateById(updateSubmit);
            throw new BusinessException(ErrorCode.API_REQUEST_ERROR, "judge queue unavailable");
        }
        return questionSubmitId;
    }

    @Override
    public QueryWrapper<QuestionSubmit> getQueryWrapper(QuestionSubmitQueryRequest questionSubmitQueryRequest) {
        QueryWrapper<QuestionSubmit> queryWrapper = new QueryWrapper<>();
        if (questionSubmitQueryRequest == null) {
            return queryWrapper;
        }
        String language = questionSubmitQueryRequest.getLanguage();
        Integer status = questionSubmitQueryRequest.getStatus();
        Long questionId = questionSubmitQueryRequest.getQuestionId();
        Long contestId = questionSubmitQueryRequest.getContestId();
        Long userId = questionSubmitQueryRequest.getUserId();
        String sortField = questionSubmitQueryRequest.getSortField();
        String sortOrder = questionSubmitQueryRequest.getSortOrder();

        queryWrapper.eq(StringUtils.isNotBlank(language), "language", language);
        queryWrapper.eq(ObjectUtils.isNotEmpty(userId), "user_id", userId);
        queryWrapper.eq(ObjectUtils.isNotEmpty(questionId), "question_id", questionId);
        queryWrapper.eq(ObjectUtils.isNotEmpty(contestId), "contest_id", contestId);
        queryWrapper.eq(QuestionSubmitStatusEnum.getEnumByValue(status) != null, "status", status);
        queryWrapper.eq("is_delete", 0);
        String effectiveSortField = sortField;
        if (StringUtils.isBlank(effectiveSortField)) {
            effectiveSortField = "submit_time";
        } else if ("createTime".equals(effectiveSortField) || "create_time".equals(effectiveSortField)) {
            effectiveSortField = "submit_time";
        } else if ("submitTime".equals(effectiveSortField)) {
            effectiveSortField = "submit_time";
        }
        boolean isAsc = CommonConstant.SORT_ORDER_ASC.equals(sortOrder);
        queryWrapper.orderBy(SqlUtils.validSortField(effectiveSortField), isAsc, effectiveSortField);
        return queryWrapper;
    }

    @Override
    public QuestionSubmitVO getQuestionSubmitVO(QuestionSubmit questionSubmit, User loginUser) {
        QuestionSubmitVO questionSubmitVO = QuestionSubmitVO.objToVo(questionSubmit);
        long userId = loginUser.getId();
        if (userId != questionSubmit.getUserId() && !userService.isAdmin(loginUser)) {
            questionSubmitVO.setCode(null);
        }
        return questionSubmitVO;
    }

    @Override
    public Page<QuestionSubmitVO> getQuestionSubmitVOPage(Page<QuestionSubmit> questionSubmitPage, User loginUser) {
        List<QuestionSubmit> questionSubmitList = questionSubmitPage.getRecords();
        Page<QuestionSubmitVO> questionSubmitVOPage = new Page<>(questionSubmitPage.getCurrent(),
                questionSubmitPage.getSize(), questionSubmitPage.getTotal());
        if (CollectionUtils.isEmpty(questionSubmitList)) {
            return questionSubmitVOPage;
        }
        List<QuestionSubmitVO> questionSubmitVOList = questionSubmitList.stream()
                .map(questionSubmit -> getQuestionSubmitVO(questionSubmit, loginUser))
                .collect(Collectors.toList());
        questionSubmitVOPage.setRecords(questionSubmitVOList);

        // 1. 关联查询用户信息
        List<Long> userIds = questionSubmitList.stream().map(QuestionSubmit::getUserId).collect(Collectors.toList());
        List<User> userList = userService.listByIds(userIds);
        // 2. 关联查询题目信息
        List<Long> questionIds = questionSubmitList.stream().map(QuestionSubmit::getQuestionId)
                .collect(Collectors.toList());
        List<Question> questionList = questionService.listByIds(questionIds);

        // 3. 填充信息
        questionSubmitVOList.forEach(questionSubmitVO -> {
            Long userId = questionSubmitVO.getUserId();
            Long questionId = questionSubmitVO.getQuestionId();
            User user = userList.stream().filter(u -> u.getId().equals(userId)).findFirst().orElse(null);
            Question question = questionList.stream().filter(q -> q.getId().equals(questionId)).findFirst()
                    .orElse(null);
            questionSubmitVO.setUserVO(userService.getUserVO(user));
            questionSubmitVO.setQuestionVO(questionService.getQuestionVO(question, null));
        });

        return questionSubmitVOPage;
    }
}
