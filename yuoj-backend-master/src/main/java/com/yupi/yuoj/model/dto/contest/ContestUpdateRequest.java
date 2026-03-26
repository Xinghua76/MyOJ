package com.yupi.yuoj.model.dto.contest;

import java.io.Serializable;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

/**
 * Update contest request
 */
@Data
public class ContestUpdateRequest implements Serializable {

    private Long id;

    private String title;

    private String description;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date startTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date endTime;

    private Integer status;

    private static final long serialVersionUID = 1L;
}
