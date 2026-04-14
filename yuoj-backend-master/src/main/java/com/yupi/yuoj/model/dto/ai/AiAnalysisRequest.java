package com.yupi.yuoj.model.dto.ai;

import lombok.Data;
import java.io.Serializable;

/**
 * AI 分析请求
 */
@Data
public class AiAnalysisRequest implements Serializable {

    /**
     * 题目 ID
     */
    private Long questionId;

    /**
     * 提交记录 ID (可选)
     */
    private Long questionSubmitId;

    /**
     * 用户代码
     */
    private String code;

    /**
     * 编程语言
     */
    private String language;

    /**
     * 判题信息 (JSON 字符串)
     */
    private String judgeInfo;

    private static final long serialVersionUID = 1L;
}
