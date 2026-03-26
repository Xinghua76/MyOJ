package com.yupi.yuoj.model.vo;

import cn.hutool.json.JSONUtil;
import com.yupi.yuoj.judge.codesandbox.model.JudgeInfo;
import com.yupi.yuoj.model.entity.QuestionSubmit;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;
import org.springframework.beans.BeanUtils;

/**
 * Question submit view
 */
@Data
public class QuestionSubmitVO implements Serializable {
    /**
     * id
     */
    private Long id;

    /**
     * language
     */
    private String language;

    /**
     * code
     */
    private String code;

    /**
     * judge info
     */
    private JudgeInfo judgeInfo;

    /**
     * status
     */
    private Integer status;

    /**
     * question id
     */
    private Long questionId;

    /**
     * user id
     */
    private Long userId;

    /**
     * submit time
     */
    private Date submitTime;

    /**
     * update time
     */
    private Date updateTime;

    /**
     * user info
     */
    private UserVO userVO;

    /**
     * question info
     */
    private QuestionVO questionVO;

    public static QuestionSubmit voToObj(QuestionSubmitVO questionSubmitVO) {
        if (questionSubmitVO == null) {
            return null;
        }
        QuestionSubmit questionSubmit = new QuestionSubmit();
        BeanUtils.copyProperties(questionSubmitVO, questionSubmit);
        JudgeInfo judgeInfoObj = questionSubmitVO.getJudgeInfo();
        if (judgeInfoObj != null) {
            questionSubmit.setJudgeInfo(JSONUtil.toJsonStr(judgeInfoObj));
        }
        return questionSubmit;
    }

    public static QuestionSubmitVO objToVo(QuestionSubmit questionSubmit) {
        if (questionSubmit == null) {
            return null;
        }
        QuestionSubmitVO questionSubmitVO = new QuestionSubmitVO();
        BeanUtils.copyProperties(questionSubmit, questionSubmitVO);
        String judgeInfoStr = questionSubmit.getJudgeInfo();
        questionSubmitVO.setJudgeInfo(JSONUtil.toBean(judgeInfoStr, JudgeInfo.class));
        return questionSubmitVO;
    }

    private static final long serialVersionUID = 1L;
}
