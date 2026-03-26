package com.yupi.yuoj.model.dto.question;

import java.io.Serializable;
import java.util.List;
import lombok.Data;

/**
 * Edit request
 */
@Data
public class QuestionEditRequest implements Serializable {

    private Long id;
    private String title;
    private String content;
    private List<String> tags;
    private String answer;
    private List<JudgeCase> judgeCase;
    private JudgeConfig judgeConfig;
    private Integer difficulty;
    private Integer status;

    private static final long serialVersionUID = 1L;
}
