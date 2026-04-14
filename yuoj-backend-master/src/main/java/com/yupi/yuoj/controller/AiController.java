package com.yupi.yuoj.controller;

import com.yupi.yuoj.common.BaseResponse;
import com.yupi.yuoj.common.ErrorCode;
import com.yupi.yuoj.common.ResultUtils;
import com.yupi.yuoj.exception.BusinessException;
import com.yupi.yuoj.manager.AiManager;
import com.yupi.yuoj.model.dto.ai.AiAnalysisRequest;
import com.yupi.yuoj.model.entity.User;
import com.yupi.yuoj.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * AI 助手接口
 */
@RestController
@RequestMapping("/ai")
@Slf4j
public class AiController {

    @Resource
    private AiManager aiManager;

    @Resource
    private UserService userService;

    /**
     * AI 分析代码并给出学习建议
     *
     * @param aiAnalysisRequest
     * @param request
     * @return
     */
    @PostMapping("/analyze")
    public BaseResponse<String> analyzeCode(@RequestBody AiAnalysisRequest aiAnalysisRequest,
            HttpServletRequest request) {
        try {
            // 参数校验
            if (aiAnalysisRequest == null) {
                log.warn("AI 分析请求体为空");
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数不能为空");
            }

            // 只有登录用户才能使用 AI 助手
            final User loginUser = userService.getLoginUser(request);
            if (loginUser == null) {
                log.warn("未登录用户尝试访问 AI 分析接口");
                throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR, "请先登录");
            }

            log.info("用户 {} 请求 AI 分析，问题 ID: {}", loginUser.getId(), aiAnalysisRequest.getQuestionId());

            // 调用 AI 助手进行分析
            String result = aiManager.analyzeCode(aiAnalysisRequest);

            if (StringUtils.isBlank(result)) {
                log.error("AI 分析为空结果，问题 ID: {}", aiAnalysisRequest.getQuestionId());
                return ResultUtils.error(ErrorCode.SYSTEM_ERROR, "AI 分析失败，请稍后重试");
            }

            log.info("AI 分析完成，用户: {}，结果长度: {} 字符", loginUser.getId(), result.length());
            return ResultUtils.success(result);

        } catch (BusinessException e) {
            log.warn("AI 分析业务异常: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("AI 分析异常", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "AI 分析服务异常，请稍后重试");
        }
    }
}
