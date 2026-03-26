package com.yupi.yuoj.model.vo;

import cn.hutool.json.JSONUtil;
import com.yupi.yuoj.model.dto.question.JudgeConfig;
import com.yupi.yuoj.model.entity.Question;
import java.io.Serializable;
import java.util.Date;
import java.util.List;
import lombok.Data;
import org.springframework.beans.BeanUtils;

/**
 * Question view
 */
@Data
public class QuestionVO implements Serializable {
    /**
     * id
     */
    private Long id;

    /**
     * title
     */
    private String title;

    /**
     * content
     */
    private String content;

    /**
     * tags
     */
    private List<String> tags;

    /**
     * difficulty
     */
    private Integer difficulty;

    /**
     * submit count
     */
    private Integer submitNum;

    /**
     * accepted count
     */
    private Integer acceptedNum;

    /**
     * judge config
     */
    private JudgeConfig judgeConfig;

    /**
     * like count
     */
    private Integer thumbNum;

    /**
     * favour count
     */
    private Integer favourNum;

    /**
     * creator id
     */
    private Long userId;

    /**
     * status
     */
    private Integer status;

    /**
     * create time
     */
    private Date createTime;

    /**
     * update time
     */
    private Date updateTime;

    /**
     * user info
     */
    private UserVO userVO;

    /**
     * answer (only for admin or creator, currently)
     */
    private String answer;

    public static Question voToObj(QuestionVO questionVO) {
        if (questionVO == null) {
            return null;
        }
        Question question = new Question();
        BeanUtils.copyProperties(questionVO, question);
        List<String> tagList = questionVO.getTags();
        if (tagList != null) {
            question.setTags(JSONUtil.toJsonStr(tagList));
        }
        JudgeConfig voJudgeConfig = questionVO.getJudgeConfig();
        if (voJudgeConfig != null) {
            question.setJudgeConfig(JSONUtil.toJsonStr(voJudgeConfig));
        }
        return question;
    }

    public static QuestionVO objToVo(Question question) {
        if (question == null) {
            return null;
        }
        QuestionVO questionVO = new QuestionVO();
        BeanUtils.copyProperties(question, questionVO);
        List<String> tagList = JSONUtil.toList(question.getTags(), String.class);
        questionVO.setTags(tagList);
        String judgeConfigStr = question.getJudgeConfig();
        questionVO.setJudgeConfig(JSONUtil.toBean(judgeConfigStr, JudgeConfig.class));
        return questionVO;
    }

    private static final long serialVersionUID = 1L;
}
