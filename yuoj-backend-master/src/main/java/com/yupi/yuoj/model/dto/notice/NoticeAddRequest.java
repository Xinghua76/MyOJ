package com.yupi.yuoj.model.dto.notice;

import java.io.Serializable;
import lombok.Data;

/**
 * Add notice request
 */
@Data
public class NoticeAddRequest implements Serializable {

    private String title;

    private String content;

    private Integer status;

    private static final long serialVersionUID = 1L;
}
