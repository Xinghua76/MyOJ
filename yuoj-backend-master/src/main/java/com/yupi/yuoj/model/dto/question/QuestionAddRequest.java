package com.yupi.yuoj.model.dto.question;

import java.io.Serializable;
import java.util.List;
import lombok.Data;

/**
 * Create request
 */
@Data
public class QuestionAddRequest implements Serializable {

    private String title;
    private String content;
    private List<String> tags;
    private String answer;
    private List<JudgeCase> judgeCase;
    private JudgeConfig judgeConfig;
    private Integer difficulty;

    private static final long serialVersionUID = 1L;
}
