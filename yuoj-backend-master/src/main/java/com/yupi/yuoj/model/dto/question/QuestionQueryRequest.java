package com.yupi.yuoj.model.dto.question;

import com.yupi.yuoj.common.PageRequest;
import java.io.Serializable;
import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Query request
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class QuestionQueryRequest extends PageRequest implements Serializable {

    /**
     * id
     */
    private Long id;

    /**
     * title
     */
    private String title;

    /**
     * content
     */
    private String content;

    /**
     * tags
     */
    private List<String> tags;

    /**
     * answer
     */
    private String answer;

    /**
     * creator id
     */
    private Long userId;

    /**
     * difficulty
     */
    private Integer difficulty;

    /**
     * status
     */
    private Integer status;

    /**
     * keyword (search in title, content, tags)
     */
    private String keyword;

    /**
     * ids for batch operations
     */
    private List<Long> ids;

    private static final long serialVersionUID = 1L;
}
