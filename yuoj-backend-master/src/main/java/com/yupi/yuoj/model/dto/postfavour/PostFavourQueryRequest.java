package com.yupi.yuoj.model.dto.postfavour;

import com.yupi.yuoj.common.PageRequest;
import com.yupi.yuoj.model.dto.post.PostQueryRequest;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class PostFavourQueryRequest extends PageRequest implements Serializable {

    private Long userId;

    private PostQueryRequest postQueryRequest;

    private static final long serialVersionUID = 1L;
}
