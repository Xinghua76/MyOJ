package com.yupi.yuoj.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yupi.yuoj.model.entity.Post;
import com.yupi.yuoj.model.entity.PostFavour;
import com.yupi.yuoj.model.entity.User;

public interface PostFavourService extends IService<PostFavour> {

    int doPostFavour(long postId, User loginUser);

    Page<Post> listFavourPostByPage(Page<Post> page, QueryWrapper<Post> queryWrapper, long favourUserId);

    int doPostFavourInner(long userId, long postId);
}
