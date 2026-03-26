package com.yupi.yuoj.model.dto.solutioncomment;

import com.yupi.yuoj.common.PageRequest;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Query solution comment request
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class SolutionCommentQueryRequest extends PageRequest implements Serializable {

    private Long id;

    private Long solutionId;

    private Long userId;

    private Long parentId;

    private Integer status;

    private static final long serialVersionUID = 1L;
}
