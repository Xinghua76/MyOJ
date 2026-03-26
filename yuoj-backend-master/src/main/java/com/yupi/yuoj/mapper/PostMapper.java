package com.yupi.yuoj.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yupi.yuoj.model.entity.Post;
import java.util.Date;
import java.util.List;
import org.apache.ibatis.annotations.Param;

/**
 * Solution mapper (reusing Post mapper)
 */
public interface PostMapper extends BaseMapper<Post> {

    /**
     * list solutions including deleted
     */
    List<Post> listPostWithDelete(@Param("minUpdateTime") Date minUpdateTime);
}
