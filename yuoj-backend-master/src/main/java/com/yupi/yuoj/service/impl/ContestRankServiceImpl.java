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
import com.yupi.yuoj.model.entity.*;
import com.yupi.yuoj.model.enums.QuestionSubmitStatusEnum;
import com.yupi.yuoj.model.vo.ContestRankVO;
import com.yupi.yuoj.model.vo.UserVO;
import com.yupi.yuoj.service.*;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Contest rank service
 */
@Service
public class ContestRankServiceImpl extends ServiceImpl<ContestRankMapper, ContestRank>
        implements ContestRankService {

    private final static Gson GSON = new Gson();

    @Resource
    private UserService userService;

    @Resource
    private QuestionSubmitService questionSubmitService;

    @Resource
    private ContestQuestionService contestQuestionService;

    @Resource
    private ContestService contestService;

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
            // If no start time, assume 0 penalty for time? Or just use submit time?
            // Usually contests have start time.
            startTime = new Date(0);
        }

        // 1. Get all submissions for this contest
        QueryWrapper<QuestionSubmit> qQuery = new QueryWrapper<>();
        qQuery.eq("contest_id", contestId);
        // Only consider valid submissions? Usually we count all for penalty
        // calculation.
        List<QuestionSubmit> submissions = questionSubmitService.list(qQuery);

        // 2. Get all questions in contest (to know scores)
        QueryWrapper<ContestQuestion> cqQuery = new QueryWrapper<>();
        cqQuery.eq("contest_id", contestId);
        List<ContestQuestion> contestQuestions = contestQuestionService.list(cqQuery);
        Map<Long, Integer> questionScoreMap = contestQuestions.stream()
                .collect(Collectors.toMap(ContestQuestion::getQuestionId,
                        cq -> cq.getScore() == null ? 0 : cq.getScore(), (k1, k2) -> k1));

        // 3. Calculate Rank
        // Map<UserId, RankInfo>
        Map<Long, ContestRankVO> rankMap = new HashMap<>();

        // Group submissions by user
        Map<Long, List<QuestionSubmit>> userSubmissions = submissions.stream()
                .collect(Collectors.groupingBy(QuestionSubmit::getUserId));

        for (Map.Entry<Long, List<QuestionSubmit>> entry : userSubmissions.entrySet()) {
            Long userId = entry.getKey();
            List<QuestionSubmit> userSubmitList = entry.getValue();

            ContestRankVO rankVO = new ContestRankVO();
            rankVO.setUserId(userId);
            rankVO.setContestId(contestId);
            rankVO.setSolvedCount(0);
            rankVO.setTotalScore(0);
            rankVO.setPenalty(0);

            // Group by question
            Map<Long, List<QuestionSubmit>> questionSubmitMap = userSubmitList.stream()
                    .collect(Collectors.groupingBy(QuestionSubmit::getQuestionId));

            for (Map.Entry<Long, List<QuestionSubmit>> qEntry : questionSubmitMap.entrySet()) {
                Long questionId = qEntry.getKey();
                List<QuestionSubmit> qSubmits = qEntry.getValue();

                // Sort by time
                qSubmits.sort(
                        Comparator.comparing(qs -> qs.getSubmitTime() == null ? new Date(0) : qs.getSubmitTime()));

                boolean isSolved = false;
                int wrongCount = 0;
                long solvedTime = 0;

                for (QuestionSubmit qs : qSubmits) {
                    if (qs.getStatus().equals(QuestionSubmitStatusEnum.SUCCEED.getValue())) {
                        String judgeInfoStr = qs.getJudgeInfo();
                        try {
                            JudgeInfo judgeInfo = GSON.fromJson(judgeInfoStr, JudgeInfo.class);
                            if (judgeInfo != null && "Accepted".equals(judgeInfo.getMessage())) {
                                isSolved = true;
                                long submitTime = qs.getSubmitTime() == null ? 0 : qs.getSubmitTime().getTime();
                                solvedTime = submitTime - startTime.getTime();
                                if (solvedTime < 0)
                                    solvedTime = 0;
                                break; // First AC counts
                            } else {
                                wrongCount++;
                            }
                        } catch (Exception e) {
                            // ignore parse error
                            wrongCount++;
                        }
                    } else {
                        // Failed or Waiting
                        // If Failed (system error), maybe ignore?
                        // If Running/Waiting, ignore.
                        if (qs.getStatus().equals(QuestionSubmitStatusEnum.FAILED.getValue())) {
                            // System error, ignore? Or wrong answer?
                            // Usually system error is ignored.
                            // But let's check judgeInfo if available.
                            // For simplicity: if status is 3 (FAILED) it might be WA if we mapped it so.
                            // But usually WA is SUCCEED status with WA message.
                            // Let's assume only non-Accepted in SUCCEED status counts as Wrong.
                        }
                    }
                }

                if (isSolved) {
                    rankVO.setSolvedCount(rankVO.getSolvedCount() + 1);
                    rankVO.setTotalScore(rankVO.getTotalScore() + questionScoreMap.getOrDefault(questionId, 100));
                    // Penalty: Time (in minutes usually, here ms) + Wrong * 20 mins
                    // Let's use seconds or minutes? Prompt says "Penalty". Usually minutes or
                    // seconds.
                    // Let's use seconds for precision.
                    long penaltySeconds = solvedTime / 1000 + wrongCount * 20 * 60;
                    rankVO.setPenalty(rankVO.getPenalty() + (int) penaltySeconds);
                }
            }
            rankMap.put(userId, rankVO);
        }

        List<ContestRankVO> rankList = new ArrayList<>(rankMap.values());

        // Sort: Solved Desc, Penalty Asc
        rankList.sort((a, b) -> {
            if (!a.getSolvedCount().equals(b.getSolvedCount())) {
                return b.getSolvedCount() - a.getSolvedCount();
            }
            return a.getPenalty() - b.getPenalty();
        });

        // Pagination
        long current = request.getCurrent();
        long size = request.getPageSize();
        long total = rankList.size();

        List<ContestRankVO> pagedList = rankList.stream()
                .skip((current - 1) * size)
                .limit(size)
                .collect(Collectors.toList());

        // Fill User Info
        Set<Long> userIds = pagedList.stream().map(ContestRankVO::getUserId).collect(Collectors.toSet());
        if (!userIds.isEmpty()) {
            Map<Long, List<User>> userMap = userService.listByIds(userIds).stream()
                    .collect(Collectors.groupingBy(User::getId));

            for (ContestRankVO vo : pagedList) {
                Long uid = vo.getUserId();
                if (userMap.containsKey(uid)) {
                    vo.setUserVO(userService.getUserVO(userMap.get(uid).get(0)));
                }
            }
        }

        Page<ContestRankVO> resultPage = new Page<>(current, size, total);
        resultPage.setRecords(pagedList);
        return resultPage;
    }
}
