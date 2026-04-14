package com.yupi.yuoj.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yupi.yuoj.annotation.AuthCheck;
import com.yupi.yuoj.common.BaseResponse;
import com.yupi.yuoj.common.DeleteRequest;
import com.yupi.yuoj.common.ErrorCode;
import com.yupi.yuoj.common.ResultUtils;
import com.yupi.yuoj.constant.UserConstant;
import com.yupi.yuoj.exception.BusinessException;
import com.yupi.yuoj.exception.ThrowUtils;
import com.yupi.yuoj.model.dto.contest.ContestQuestionAddRequest;
import com.yupi.yuoj.model.dto.contest.ContestQuestionQueryRequest;
import com.yupi.yuoj.model.entity.Contest;
import com.yupi.yuoj.model.entity.ContestQuestion;
import com.yupi.yuoj.model.entity.ContestSignup;
import com.yupi.yuoj.model.entity.User;
import com.yupi.yuoj.model.vo.ContestQuestionVO;
import com.yupi.yuoj.service.ContestQuestionService;
import com.yupi.yuoj.service.ContestService;
import com.yupi.yuoj.service.ContestSignupService;
import com.yupi.yuoj.service.UserService;
import java.util.Date;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Contest question endpoints
 */
@RestController
@RequestMapping("/contest_question")
public class ContestQuestionController {

    @Resource
    private ContestQuestionService contestQuestionService;

    @Resource
    private ContestService contestService;

    @Resource
    private ContestSignupService contestSignupService;

    @Resource
    private UserService userService;

    @PostMapping("/add")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Long> add(@RequestBody ContestQuestionAddRequest request) {
        if (request == null || request.getContestId() == null || request.getQuestionId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        if (request.getScore() != null && request.getScore() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "score must be positive");
        }
        if (request.getOrderNo() != null && request.getOrderNo() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "orderNo must be positive");
        }
        ContestQuestionQueryRequest queryRequest = new ContestQuestionQueryRequest();
        queryRequest.setContestId(request.getContestId());
        queryRequest.setQuestionId(request.getQuestionId());
        ContestQuestion existed = contestQuestionService.getOne(contestQuestionService.getQueryWrapper(queryRequest));
        if (existed != null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "question already added");
        }
        ContestQuestion contestQuestion = new ContestQuestion();
        BeanUtils.copyProperties(request, contestQuestion);
        if (contestQuestion.getScore() == null) {
            contestQuestion.setScore(100);
        }
        if (contestQuestion.getOrderNo() == null) {
            contestQuestion.setOrderNo(1);
        }
        boolean result = contestQuestionService.save(contestQuestion);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(contestQuestion.getId());
    }

    @PostMapping("/delete")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> delete(@RequestBody DeleteRequest request) {
        if (request == null || request.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean result = contestQuestionService.removeById(request.getId());
        return ResultUtils.success(result);
    }

    @PostMapping("/list/page")
    public BaseResponse<Page<ContestQuestionVO>> listContestQuestionByPage(
            @RequestBody ContestQuestionQueryRequest request,
            HttpServletRequest httpRequest) {
        if (request == null || request.getContestId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        long contestId = request.getContestId();
        Contest contest = contestService.getById(contestId);
        if (contest == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "contest not found");
        }

        User loginUser = userService.getLoginUserPermitNull(httpRequest);
        boolean isAdmin = loginUser != null && userService.isAdmin(loginUser);
        Date now = new Date();
        boolean contestStarted = contest.getStartTime() == null || !now.before(contest.getStartTime());
        boolean contestEnded = contest.getEndTime() != null && now.after(contest.getEndTime());

        if (!isAdmin) {
            if (!contestStarted) {
                throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "contest not started");
            }
            if (!contestEnded) {
                if (loginUser == null) {
                    throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
                }
                QueryWrapper<ContestSignup> signupQuery = new QueryWrapper<>();
                signupQuery.eq("contest_id", contestId);
                signupQuery.eq("user_id", loginUser.getId());
                signupQuery.eq("status", 1);
                long count = contestSignupService.count(signupQuery);
                if (count == 0) {
                    throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "not signed up");
                }
            }
        }

        long current = request.getCurrent();
        long size = request.getPageSize();
        Page<ContestQuestion> page = contestQuestionService.page(new Page<>(current, size),
                contestQuestionService.getQueryWrapper(request));
        return ResultUtils.success(contestQuestionService.getContestQuestionVOPage(page, httpRequest));
    }
}
