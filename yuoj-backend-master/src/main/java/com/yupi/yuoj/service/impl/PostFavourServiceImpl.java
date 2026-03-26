package com.yupi.yuoj.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yupi.yuoj.common.ErrorCode;
import com.yupi.yuoj.exception.BusinessException;
import com.yupi.yuoj.mapper.PostFavourMapper;
import com.yupi.yuoj.model.entity.Post;
import com.yupi.yuoj.model.entity.PostFavour;
import com.yupi.yuoj.model.entity.User;
import com.yupi.yuoj.service.PostFavourService;
import com.yupi.yuoj.service.PostService;
import java.util.List;
import javax.annotation.Resource;
import org.springframework.aop.framework.AopContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

@Service
public class PostFavourServiceImpl extends ServiceImpl<PostFavourMapper, PostFavour> implements PostFavourService {

    @Resource
    private PostService postService;

    @Override
    public int doPostFavour(long postId, User loginUser) {
        Post post = postService.getById(postId);
        if (post == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        long userId = loginUser.getId();
        PostFavourService postFavourService = (PostFavourService) AopContext.currentProxy();
        synchronized (String.valueOf(userId).intern()) {
            return postFavourService.doPostFavourInner(userId, postId);
        }
    }

    @Override
    public Page<Post> listFavourPostByPage(Page<Post> page, QueryWrapper<Post> queryWrapper, long favourUserId) {
        if (favourUserId <= 0) {
            return new Page<>();
        }
        QueryWrapper<PostFavour> favourQueryWrapper = new QueryWrapper<>();
        favourQueryWrapper.select("post_id");
        favourQueryWrapper.eq("user_id", favourUserId);
        List<Object> postIdList = this.listObjs(favourQueryWrapper);
        if (CollectionUtils.isEmpty(postIdList)) {
            return new Page<>();
        }
        queryWrapper.in("id", postIdList);
        return postService.page(page, queryWrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int doPostFavourInner(long userId, long postId) {
        QueryWrapper<PostFavour> favourQueryWrapper = new QueryWrapper<>();
        favourQueryWrapper.eq("user_id", userId);
        favourQueryWrapper.eq("post_id", postId);
        PostFavour oldPostFavour = this.getOne(favourQueryWrapper);
        boolean result;
        if (oldPostFavour != null) {
            result = this.remove(favourQueryWrapper);
            if (result) {
                result = postService.update().setSql("favour_num = favour_num - 1").eq("id", postId)
                        .gt("favour_num", 0).update();
                return result ? -1 : 0;
            }
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        }
        PostFavour postFavour = new PostFavour();
        postFavour.setUserId(userId);
        postFavour.setPostId(postId);
        result = this.save(postFavour);
        if (result) {
            result = postService.update().setSql("favour_num = favour_num + 1").eq("id", postId).update();
            return result ? 1 : 0;
        }
        throw new BusinessException(ErrorCode.SYSTEM_ERROR);
    }
}
