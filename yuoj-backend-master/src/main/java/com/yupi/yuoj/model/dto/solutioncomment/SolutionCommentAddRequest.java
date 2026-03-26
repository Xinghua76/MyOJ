package com.yupi.yuoj.model.dto.solutioncomment;

import java.io.Serializable;
import lombok.Data;

/**
 * Add solution comment request
 */
@Data
public class SolutionCommentAddRequest implements Serializable {

    private Long solutionId;

    private String content;

    private Long parentId;

    private static final long serialVersionUID = 1L;
}
