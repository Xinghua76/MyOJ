package com.yupi.yuoj.model.vo;

import com.yupi.yuoj.model.entity.Contest;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;
import org.springframework.beans.BeanUtils;

/**
 * Contest view
 */
@Data
public class ContestVO implements Serializable {

    private Long id;

    private String title;

    private String description;

    private Date startTime;

    private Date endTime;

    private Integer status;

    private Long creatorId;

    private Date createTime;

    private Date updateTime;

    private UserVO creatorVO;

    public static ContestVO objToVo(Contest contest) {
        if (contest == null) {
            return null;
        }
        ContestVO vo = new ContestVO();
        BeanUtils.copyProperties(contest, vo);
        
        // Dynamically calculate status based on time
        if (contest.getStartTime() != null && contest.getEndTime() != null) {
            long now = System.currentTimeMillis();
            long start = contest.getStartTime().getTime();
            long end = contest.getEndTime().getTime();
            if (now < start) {
                vo.setStatus(0);
            } else if (now > end) {
                vo.setStatus(2);
            } else {
                vo.setStatus(1);
            }
        }
        
        return vo;
    }

    private static final long serialVersionUID = 1L;
}
