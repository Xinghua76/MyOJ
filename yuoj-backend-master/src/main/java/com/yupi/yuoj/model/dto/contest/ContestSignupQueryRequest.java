package com.yupi.yuoj.model.dto.contest;

import com.yupi.yuoj.common.PageRequest;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Query contest signup request
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class ContestSignupQueryRequest extends PageRequest implements Serializable {

    private Long contestId;

    private Long userId;

    private Integer status;

    private static final long serialVersionUID = 1L;
}
