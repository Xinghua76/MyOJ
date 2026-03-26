package com.yupi.yuoj.model.dto.contest;

import com.yupi.yuoj.common.PageRequest;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Query contest request
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class ContestQueryRequest extends PageRequest implements Serializable {

    private Long id;

    private String title;

    private Integer status;

    private Long creatorId;

    private static final long serialVersionUID = 1L;
}
