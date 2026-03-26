package com.yupi.yuoj.model.dto.solutioncomment;

import java.io.Serializable;
import lombok.Data;

/**
 * Update solution comment request
 */
@Data
public class SolutionCommentUpdateRequest implements Serializable {

    private Long id;

    private String content;

    private Integer status;

    private static final long serialVersionUID = 1L;
}
