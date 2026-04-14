package com.yupi.yuoj.manager;

import cn.hutool.http.HttpRequest;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.yupi.yuoj.common.ErrorCode;
import com.yupi.yuoj.exception.BusinessException;
import com.yupi.yuoj.model.dto.ai.AiAnalysisRequest;
import com.yupi.yuoj.model.entity.Question;
import com.yupi.yuoj.service.QuestionService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * AI 助手管理类
 * 已集成 DeepSeek API
 */
@Service
@Slf4j
public class AiManager {

    @Resource
    private QuestionService questionService;

    @Value("${ai.deepseek.api-key:}")
    private String apiKey;

    @Value("${ai.deepseek.model:deepseek-chat}")
    private String model;

    @Value("${ai.deepseek.url:https://api.deepseek.com/v1/chat/completions}")
    private String apiUrl;

    /**
     * AI 分析代码并给出学习建议
     *
     * @param aiAnalysisRequest
     * @return
     */
    public String analyzeCode(AiAnalysisRequest aiAnalysisRequest) {
        // 参数验证
        if (aiAnalysisRequest == null) {
            log.error("AI 分析请求参数为空");
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "分析请求不能为空");
        }

        Long questionId = aiAnalysisRequest.getQuestionId();
        String code = aiAnalysisRequest.getCode();
        String language = aiAnalysisRequest.getLanguage();
        String judgeInfoStr = aiAnalysisRequest.getJudgeInfo();

        // 验证问题 ID
        if (questionId == null || questionId <= 0) {
            log.warn("问题 ID 无效: {}", questionId);
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "问题 ID 不能为空");
        }

        // 验证代码
        if (StringUtils.isBlank(code)) {
            log.warn("用户代码为空");
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "代码内容不能为空");
        }

        // 验证编程语言
        if (StringUtils.isBlank(language)) {
            log.warn("编程语言未指定");
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "编程语言不能为空");
        }

        // 代码长度限制（防止恶意输入）
        if (code.length() > 50000) {
            log.warn("代码长度超过限制: {} 字符", code.length());
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "代码长度不能超过 50000 字符");
        }

        Question question = questionService.getById(questionId);
        if (question == null) {
            log.warn("问题不存在，ID: {}", questionId);
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "题目不存在或已删除");
        }

        String systemPrompt = buildSystemPrompt();
        String userPrompt = buildUserPrompt(question, language, judgeInfoStr, code);

        // 如果没有配置 API Key，则回退到 Mock 逻辑
        if (StringUtils.isBlank(apiKey) || "your-api-key-here".equals(apiKey)) {
            log.warn("DeepSeek API Key 未配置，将使用 Mock 逻辑。");
            return getMockFeedback(judgeInfoStr);
        }

        return callDeepSeekApi(systemPrompt, userPrompt, judgeInfoStr);
    }

    /**
     * 系统提示词：定义 AI 导师角色、边界与输出格式
     */
    private String buildSystemPrompt() {
        return "你是 OJ 平台的资深编程导师，目标是帮助学生独立定位问题并优化代码。\n"
                + "请严格遵守：\n"
                + "1) 不直接给出完整可提交答案，不提供整段可直接 AC 的最终代码。\n"
            + "2) 不要预设用户代码一定错误，先做正确性判断，再给出对应建议。\n"
                + "3) 建议必须结合用户代码与判题信息，不要泛泛而谈。\n"
                + "4) 使用简体中文 Markdown 输出，结构清晰、可执行。\n"
                + "5) 若信息不足，请明确说明需要补充的输入。\n"
            + "6) 避免编造题目约束、样例与接口。\n"
            + "7) 若代码可能已正确（如已通过或未见明确错误），重点给出鲁棒性、复杂度与可读性优化建议。";
    }

    /**
     * 用户提示词：注入题目、判题与代码上下文
     */
    private String buildUserPrompt(Question question, String language, String judgeInfoStr, String code) {
        String safeJudgeInfo = StringUtils.defaultIfBlank(judgeInfoStr, "暂无判题信息");

        StringBuilder prompt = new StringBuilder();
        prompt.append("请基于以下上下文进行诊断与指导：\n\n");
        prompt.append("# 题目信息\n");
        prompt.append("- 标题：").append(question.getTitle()).append("\n");
        prompt.append("- 内容：\n").append(question.getContent()).append("\n\n");

        prompt.append("# 提交信息\n");
        prompt.append("- 编程语言：").append(language).append("\n");
        prompt.append("- 判题结果：").append(safeJudgeInfo).append("\n\n");

        prompt.append("# 用户代码\n");
        prompt.append("```\n").append(code).append("\n```\n\n");

        prompt.append("请严格按下列结构输出（Markdown）：\n");
        prompt.append("## 1. 正确性判断\n");
        prompt.append("- 先判断该代码更可能是“基本正确 / 可能有误 / 信息不足”，并给出依据（结合判题信息与代码）。\n");
        prompt.append("- 如果无法确定，请明确不确定点。\n\n");

        prompt.append("## 2. 问题定位（仅在存在明显问题时）\n");
        prompt.append("- 若发现问题，指出最可能导致失败的 1~3 个关键点，并引用对应代码片段或逻辑位置。\n");
        prompt.append("- 若未发现明确错误，写“未发现明确错误”，不要强行编造问题。\n\n");

        prompt.append("## 3. 改进思路\n");
        prompt.append("- 若有错误：给出逐步修复方案（先做什么、再做什么）。\n");
        prompt.append("- 若代码基本正确：给出性能、鲁棒性、可读性三方面的优化建议。\n");
        prompt.append("- 若涉及复杂度，说明当前复杂度与目标复杂度。\n\n");

        prompt.append("## 4. 关键知识点\n");
        prompt.append("- 用 3~5 条要点解释该题相关算法/数据结构与易错点。\n\n");

        prompt.append("## 5. 自检清单\n");
        prompt.append("- 给出提交前可执行的检查项（边界值、特殊输入、输出格式等）。\n\n");

        prompt.append("## 6. 引导提问\n");
        prompt.append("- 追加 2 个问题，引导用户自己验证修复是否正确。\n");

        return prompt.toString();
    }

    /**
     * 调用 DeepSeek API
     *
     * @param systemPrompt
     * @param userPrompt
     * @param judgeInfoStr
     * @return
     */
    private String callDeepSeekApi(String systemPrompt, String userPrompt, String judgeInfoStr) {
        try {
            // 构造请求体
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", model);

            List<Map<String, String>> messages = new ArrayList<>();

            Map<String, String> systemMessage = new HashMap<>();
            systemMessage.put("role", "system");
            systemMessage.put("content", systemPrompt);
            messages.add(systemMessage);

            Map<String, String> userMessage = new HashMap<>();
            userMessage.put("role", "user");
            userMessage.put("content", userPrompt);
            messages.add(userMessage);

            requestBody.put("messages", messages);
            requestBody.put("stream", false);

            log.info("正在调用 DeepSeek API，Model: {}, URL: {}", model, apiUrl);

            // 发送请求
            String responseStr = HttpRequest.post(apiUrl)
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .timeout(60000) // 设置 60 秒超时
                    .body(JSONUtil.toJsonStr(requestBody))
                    .execute()
                    .body();

            log.debug("DeepSeek API 原始响应: {}", responseStr);

            JSONObject responseJson = JSONUtil.parseObj(responseStr);

            // 成功响应处理
            if (responseJson.containsKey("choices")) {
                JSONArray choices = responseJson.getJSONArray("choices");
                if (choices != null && choices.size() > 0) {
                    JSONObject firstChoice = choices.getJSONObject(0);
                    JSONObject messageJson = firstChoice.getJSONObject("message");
                    String result = messageJson.getStr("content");
                    log.info("DeepSeek API 调用成功，返回内容长度: {} 字符", result.length());
                    return result;
                }
            }

            // 错误响应处理
            if (responseJson.containsKey("error")) {
                JSONObject errorJson = responseJson.getJSONObject("error");
                String errorType = errorJson.getStr("type");
                String errorMessage = errorJson.getStr("message", "未知错误");

                log.error("DeepSeek API 错误 [{}]: {}", errorType, errorMessage);

                // 如果是认证错误，自动回退到 Mock 逻辑
                if ("authentication_error".equals(errorType) || "invalid_request_error".equals(errorType)) {
                    log.warn("DeepSeek API 认证/请求失败，自动回退到 Mock 反馈。错误: {}", errorMessage);
                    return "⚠️ DeepSeek API 暂时不可用，为你提供系统预设建议：\n\n" +
                            getMockFeedback(judgeInfoStr);
                }

                if ("rate_limit_error".equals(errorType)) {
                    log.warn("DeepSeek API 请求过于频繁，请稍后再试");
                    return "⚠️ 请求过于频繁，请稍后再试。" + getMockFeedback(judgeInfoStr);
                }

                return "AI 导师暂时无法回复，错误信息：" + errorMessage + getMockFeedback(judgeInfoStr);
            }

            log.error("DeepSeek API 返回异常响应: {}", responseStr);
            return "AI 导师暂时无法回复，请稍后再试。" + getMockFeedback(judgeInfoStr);

        } catch (Exception e) {
            log.error("DeepSeek API 调用异常", e);
            // 发生异常也尝试回退到 Mock 逻辑
            return "⚠️ AI 服务调用异常，为你提供系统预设建议：\n\n" + getMockFeedback(judgeInfoStr);
        }
    }

    /**
     * Mock 反馈逻辑 (兜底)
     */
    private String getMockFeedback(String judgeInfoStr) {
        if (StringUtils.contains(judgeInfoStr, "Accepted")) {
            return "🎉 恭喜！你的代码已经通过了所有测试。但我们仍然建议你思考：\n" +
                    "1. 是否有更优的时间或空间复杂度？\n" +
                    "2. 代码的可读性和命名是否规范？\n" +
                    "3. 如果输入规模增加 10 倍，你的方案还能跑通吗？";
        } else if (StringUtils.contains(judgeInfoStr, "Wrong Answer")) {
            return "⚠️ 判题结果为“答案错误”。\n" +
                    "1. 检查是否漏掉了某些边界情况（如空输入、极大值、负数等）。\n" +
                    "2. 题目要求的是精确匹配还是大致正确？注意输出格式。\n" +
                    "3. 核心知识点：可以复习一下“分治”或“动态规划”的细节处理。";
        } else if (StringUtils.contains(judgeInfoStr, "Time Limit Exceeded")) {
            return "⏳ 你的代码超时了。\n" +
                    "1. 当前的时间复杂度是多少？是否有重复计算？\n" +
                    "2. 尝试使用哈希表（Map）来优化搜索，或者使用动态规划代替递归。\n" +
                    "3. 核心知识点：掌握常见的算法复杂度估算方法。";
        } else {
            return "AI 导师建议：\n" +
                    "你的代码在执行中遇到了一些问题。建议从基础的逻辑流程梳理开始，\n" +
                    "确认你的思路是否完全匹配题目的要求。\n" +
                    "如果需要更详细的分析，请集成真正的 LLM 服务。";
        }
    }
}
