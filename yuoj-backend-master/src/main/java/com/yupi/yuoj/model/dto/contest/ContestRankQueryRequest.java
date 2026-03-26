package com.yupi.yuoj.model.dto.contest;

import com.yupi.yuoj.common.PageRequest;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Query contest rank request
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class ContestRankQueryRequest extends PageRequest implements Serializable {

    private Long contestId;

    private Long userId;

    private static final long serialVersionUID = 1L;
}
