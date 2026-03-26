package com.yupi.yuoj.model.vo;

import com.yupi.yuoj.model.entity.SolutionComment;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;
import org.springframework.beans.BeanUtils;

/**
 * Solution comment view
 */
@Data
public class SolutionCommentVO implements Serializable {

    private Long id;

    private Long solutionId;

    private Long userId;

    private String content;

    private Long parentId;

    private Integer likeNum;

    private Integer status;

    private Date createTime;

    private Date updateTime;

    private UserVO userVO;

    /**
     * 是否已点赞
     */
    private Boolean hasThumb;

    public static SolutionCommentVO objToVo(SolutionComment comment) {
        if (comment == null) {
            return null;
        }
        SolutionCommentVO vo = new SolutionCommentVO();
        BeanUtils.copyProperties(comment, vo);
        return vo;
    }

    private static final long serialVersionUID = 1L;
}
