package com.yupi.yuoj.model.vo;

import com.yupi.yuoj.model.entity.Notice;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;
import org.springframework.beans.BeanUtils;

/**
 * Notice view
 */
@Data
public class NoticeVO implements Serializable {

    private Long id;

    private String title;

    private String content;

    private Long publisherId;

    private Integer status;

    private Date publishTime;

    private Date updateTime;

    private UserVO publisherVO;

    public static NoticeVO objToVo(Notice notice) {
        if (notice == null) {
            return null;
        }
        NoticeVO vo = new NoticeVO();
        BeanUtils.copyProperties(notice, vo);
        return vo;
    }

    private static final long serialVersionUID = 1L;
}
