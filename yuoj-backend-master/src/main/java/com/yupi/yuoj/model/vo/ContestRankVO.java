package com.yupi.yuoj.model.vo;

import com.yupi.yuoj.model.entity.ContestRank;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.io.Serializable;

/**
 * 比赛排行榜 VO
 */
@Data
public class ContestRankVO implements Serializable {

    private Integer rank;

    private Long id;

    private Long contestId;

    private Long userId;

    private Integer solvedCount;

    private Integer totalScore;

    private Integer penalty;

    private UserVO userVO;

    private static final long serialVersionUID = 1L;

    public static ContestRankVO objToVo(ContestRank contestRank) {
        if (contestRank == null) {
            return null;
        }
        ContestRankVO contestRankVO = new ContestRankVO();
        BeanUtils.copyProperties(contestRank, contestRankVO);
        return contestRankVO;
    }
}
