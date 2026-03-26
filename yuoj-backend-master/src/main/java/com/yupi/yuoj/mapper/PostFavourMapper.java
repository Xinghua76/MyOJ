package com.yupi.yuoj.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yupi.yuoj.model.entity.PostFavour;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.yupi.yuoj.model.entity.Post;
import org.apache.ibatis.annotations.Param;

/**
 * 帖子收藏数据库操作
 */
public interface PostFavourMapper extends BaseMapper<PostFavour> {

    /**
     * 分页查询收藏帖子列表
     *
     * @param page
     * @param queryWrapper
     * @param favourUserId
     * @return
     */
    IPage<Post> listFavourPostByPage(IPage<Post> page, @Param(Constants.WRAPPER) Wrapper<Post> queryWrapper,
            @Param("favourUserId") long favourUserId);
}