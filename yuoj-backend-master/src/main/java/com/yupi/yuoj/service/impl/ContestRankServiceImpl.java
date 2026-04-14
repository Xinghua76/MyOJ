package com.yupi.yuoj.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.gson.Gson;
import com.yupi.yuoj.common.ErrorCode;
import com.yupi.yuoj.exception.BusinessException;
import com.yupi.yuoj.judge.codesandbox.model.JudgeInfo;
import com.yupi.yuoj.mapper.ContestRankMapper;
import com.yupi.yuoj.model.dto.contest.ContestRankQueryRequest;
import com.yupi.yuoj.model.entity.Contest;
import com.yupi.yuoj.model.entity.ContestQuestion;
import com.yupi.yuoj.model.entity.ContestRank;
import com.yupi.yuoj.model.entity.ContestSignup;
import com.yupi.yuoj.model.entity.QuestionSubmit;
import com.yupi.yuoj.model.entity.User;
import com.yupi.yuoj.model.enums.QuestionSubmitStatusEnum;
import com.yupi.yuoj.model.vo.ContestRankVO;
import com.yupi.yuoj.service.ContestQuestionService;
import com.yupi.yuoj.service.ContestRankService;
import com.yupi.yuoj.service.ContestService;
import com.yupi.yuoj.service.ContestSignupService;
import com.yupi.yuoj.service.QuestionSubmitService;
import com.yupi.yuoj.service.UserService;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Resource;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Service;

/**
 * Contest rank service
 */
@Service
public class ContestRankServiceImpl extends ServiceImpl<ContestRankMapper, ContestRank>
        implements ContestRankService {

    private static final Gson GSON = new Gson();

    @Resource
    private UserService userService;

    @Resource
    private QuestionSubmitService questionSubmitService;

    @Resource
    private ContestQuestionService contestQuestionService;

    @Resource
    private ContestService contestService;

    @Resource
    private ContestSignupService contestSignupService;

    @Override
    public QueryWrapper<ContestRank> getQueryWrapper(ContestRankQueryRequest request) {
        QueryWrapper<ContestRank> queryWrapper = new QueryWrapper<>();
        if (request == null) {
            return queryWrapper;
        }
        Long contestId = request.getContestId();
        Long userId = request.getUserId();
        queryWrapper.eq(ObjectUtils.isNotEmpty(contestId), "contest_id", contestId);
        queryWrapper.eq(ObjectUtils.isNotEmpty(userId), "user_id", userId);
        return queryWrapper;
    }

    @Override
    public Page<ContestRankVO> listContestRankVOByPage(ContestRankQueryRequest request) {
        Long contestId = request.getContestId();
        if (contestId == null || contestId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Contest contest = contestService.getById(contestId);
        if (contest == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }

        Date startTime = contest.getStartTime();
        if (startTime == null) {
            startTime = new Date(0);
        }

        QueryWrapper<ContestSignup> signupQuery = new QueryWrapper<>();
        signupQuery.eq("contest_id", contestId);
        signupQuery.eq("status", 1);
        List<ContestSignup> activeSignups = contestSignupService.list(signupQuery);

        QueryWrapper<QuestionSubmit> submitQuery = new QueryWrapper<>();
        submitQuery.eq("contest_id", contestId);
        List<QuestionSubmit> submissions = questionSubmitService.list(submitQuery);

        QueryWrapper<ContestQuestion> contestQuestionQuery = new QueryWrapper<>();
        contestQuestionQuery.eq("contest_id", contestId);
        List<ContestQuestion> contestQuestions = contestQuestionService.list(contestQuestionQuery);
        Map<Long, Integer> questionScoreMap = contestQuestions.stream()
                .collect(Collectors.toMap(ContestQuestion::getQuestionId,
                        question -> question.getScore() == null ? 0 : question.getScore(), (left, right) -> left));

        Map<Long, ContestRankVO> rankMap = new HashMap<>();
        for (ContestSignup signup : activeSignups) {
            rankMap.put(signup.getUserId(), initRankVO(contestId, signup.getUserId()));
        }

        Map<Long, List<QuestionSubmit>> userSubmissions = submissions.stream()
                .collect(Collectors.groupingBy(QuestionSubmit::getUserId));

        for (Map.Entry<Long, List<QuestionSubmit>> entry : userSubmissions.entrySet()) {
            Long userId = entry.getKey();
            ContestRankVO rankVO = rankMap.computeIfAbsent(userId, key -> initRankVO(contestId, key));
            Map<Long, List<QuestionSubmit>> questionSubmitMap = entry.getValue().stream()
                    .collect(Collectors.groupingBy(QuestionSubmit::getQuestionId));

            for (Map.Entry<Long, List<QuestionSubmit>> questionEntry : questionSubmitMap.entrySet()) {
                Long questionId = questionEntry.getKey();
                List<QuestionSubmit> questionSubmits = questionEntry.getValue();
                questionSubmits.sort(
                        Comparator.comparing(submit -> submit.getSubmitTime() == null ? new Date(0) : submit.getSubmitTime()));

                int wrongCount = 0;
                Long solvedAtSeconds = null;
                for (QuestionSubmit submit : questionSubmits) {
                    if (QuestionSubmitStatusEnum.WAITING.getValue().equals(submit.getStatus())
                            || QuestionSubmitStatusEnum.RUNNING.getValue().equals(submit.getStatus())) {
                        continue;
                    }
                    if (isAccepted(submit)) {
                        long submitTime = submit.getSubmitTime() == null ? 0 : submit.getSubmitTime().getTime();
                        long solvedTime = Math.max(0, submitTime - startTime.getTime());
                        solvedAtSeconds = solvedTime / 1000;
                        break;
                    }
                    if (isPenaltySubmission(submit)) {
                        wrongCount++;
                    }
                }

                if (solvedAtSeconds != null) {
                    rankVO.setSolvedCount(rankVO.getSolvedCount() + 1);
                    rankVO.setTotalScore(rankVO.getTotalScore() + questionScoreMap.getOrDefault(questionId, 100));
                    long penalty = solvedAtSeconds + wrongCount * 20L * 60;
                    rankVO.setPenalty(rankVO.getPenalty() + (int) penalty);
                }
            }
        }

        List<ContestRankVO> rankList = new ArrayList<>(rankMap.values());
        rankList.sort((left, right) -> {
            if (!left.getTotalScore().equals(right.getTotalScore())) {
                return right.getTotalScore() - left.getTotalScore();
            }
            if (!left.getSolvedCount().equals(right.getSolvedCount())) {
                return right.getSolvedCount() - left.getSolvedCount();
            }
            if (!left.getPenalty().equals(right.getPenalty())) {
                return left.getPenalty() - right.getPenalty();
            }
            return Long.compare(left.getUserId(), right.getUserId());
        });

        for (int i = 0; i < rankList.size(); i++) {
            rankList.get(i).setRank(i + 1);
        }

        long current = request.getCurrent();
        long size = request.getPageSize();
        long total = rankList.size();
        List<ContestRankVO> pagedList = rankList.stream()
                .skip((current - 1) * size)
                .limit(size)
                .collect(Collectors.toList());

        fillUserInfo(pagedList);

        Page<ContestRankVO> resultPage = new Page<>(current, size, total);
        resultPage.setRecords(pagedList);
        return resultPage;
    }

    private ContestRankVO initRankVO(Long contestId, Long userId) {
        ContestRankVO rankVO = new ContestRankVO();
        rankVO.setContestId(contestId);
        rankVO.setUserId(userId);
        rankVO.setSolvedCount(0);
        rankVO.setTotalScore(0);
        rankVO.setPenalty(0);
        return rankVO;
    }

    private boolean isAccepted(QuestionSubmit submit) {
        if (!QuestionSubmitStatusEnum.SUCCEED.getValue().equals(submit.getStatus())) {
            return false;
        }
        try {
            JudgeInfo judgeInfo = GSON.fromJson(submit.getJudgeInfo(), JudgeInfo.class);
            return judgeInfo != null && "Accepted".equals(judgeInfo.getMessage());
        } catch (Exception e) {
            return false;
        }
    }

    private boolean isPenaltySubmission(QuestionSubmit submit) {
        if (!QuestionSubmitStatusEnum.SUCCEED.getValue().equals(submit.getStatus())) {
            return false;
        }
        try {
            JudgeInfo judgeInfo = GSON.fromJson(submit.getJudgeInfo(), JudgeInfo.class);
            return judgeInfo == null || !"Accepted".equals(judgeInfo.getMessage());
        } catch (Exception e) {
            return true;
        }
    }

    private void fillUserInfo(List<ContestRankVO> rankList) {
        Set<Long> userIds = rankList.stream().map(ContestRankVO::getUserId).collect(Collectors.toSet());
        if (userIds.isEmpty()) {
            return;
        }
        Map<Long, List<User>> userMap = userService.listByIds(userIds).stream()
                .collect(Collectors.groupingBy(User::getId));
        for (ContestRankVO rankVO : rankList) {
            List<User> users = userMap.get(rankVO.getUserId());
            if (users != null && !users.isEmpty()) {
                rankVO.setUserVO(userService.getUserVO(users.get(0)));
            }
        }
    }
}
