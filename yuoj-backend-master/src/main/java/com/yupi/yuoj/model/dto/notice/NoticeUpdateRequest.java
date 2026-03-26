package com.yupi.yuoj.model.dto.notice;

import java.io.Serializable;
import lombok.Data;

/**
 * Update notice request
 */
@Data
public class NoticeUpdateRequest implements Serializable {

    private Long id;

    private String title;

    private String content;

    private Integer status;

    private static final long serialVersionUID = 1L;
}
