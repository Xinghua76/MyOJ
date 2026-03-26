package com.yupi.yuoj.model.vo;

import com.yupi.yuoj.model.entity.ContestQuestion;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.io.Serializable;

/**
 * 比赛题目 VO
 */
@Data
public class ContestQuestionVO implements Serializable {

    private Long id;

    private Long contestId;

    private Long questionId;

    private Integer score;

    private Integer orderNo;

    private QuestionVO questionVO;

    private static final long serialVersionUID = 1L;

    public static ContestQuestionVO objToVo(ContestQuestion contestQuestion) {
        if (contestQuestion == null) {
            return null;
        }
        ContestQuestionVO contestQuestionVO = new ContestQuestionVO();
        BeanUtils.copyProperties(contestQuestion, contestQuestionVO);
        return contestQuestionVO;
    }
}
