package com.yupi.yuoj.model.dto.post;

import java.io.Serializable;
import java.util.List;
import lombok.Data;

/**
 * 创建请求
 */
@Data
public class PostAddRequest implements Serializable {

    /**
     * 帖子类型（discussion / solution）
     */
    private String postType;

    /**
     * 关联题目 id（仅题解需要）
     */
    private Long questionId;

    /**
     * 标题
     */
    private String title;

    /**
     * 内容
     */
    private String content;

    /**
     * 标签列表
     */
    private List<String> tags;

    private static final long serialVersionUID = 1L;
}