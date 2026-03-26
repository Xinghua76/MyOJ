package com.yupi.yuoj.model.dto.notice;

import com.yupi.yuoj.common.PageRequest;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Query notice request
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class NoticeQueryRequest extends PageRequest implements Serializable {

    private Long id;

    private String title;

    private Integer status;

    private Long publisherId;

    private static final long serialVersionUID = 1L;
}
