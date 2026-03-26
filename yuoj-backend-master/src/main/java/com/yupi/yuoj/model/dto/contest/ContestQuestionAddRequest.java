package com.yupi.yuoj.model.dto.contest;

import java.io.Serializable;
import lombok.Data;

/**
 * Add contest question request
 */
@Data
public class ContestQuestionAddRequest implements Serializable {

    private Long contestId;

    private Long questionId;

    private Integer score;

    private Integer orderNo;

    private static final long serialVersionUID = 1L;
}
