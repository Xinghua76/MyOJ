package com.yupi.yuoj.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yupi.yuoj.common.ErrorCode;
import com.yupi.yuoj.constant.CommonConstant;
import com.yupi.yuoj.exception.BusinessException;
import com.yupi.yuoj.exception.ThrowUtils;
import com.yupi.yuoj.mapper.PostMapper;
import com.yupi.yuoj.model.dto.post.PostQueryRequest;
import com.yupi.yuoj.model.entity.Post;
import com.yupi.yuoj.model.entity.PostFavour;
import com.yupi.yuoj.model.entity.PostThumb;
import com.yupi.yuoj.model.entity.User;
import com.yupi.yuoj.model.vo.PostVO;
import com.yupi.yuoj.model.vo.UserVO;
import com.yupi.yuoj.mapper.PostFavourMapper;
import com.yupi.yuoj.mapper.PostThumbMapper;
import com.yupi.yuoj.service.PostService;
import com.yupi.yuoj.service.UserService;
import com.yupi.yuoj.utils.SqlUtils;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.HashSet;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

/**
 * Solution service (reusing Post module)
 */
@Service
public class PostServiceImpl extends ServiceImpl<PostMapper, Post> implements PostService {

    private static final String POST_TYPE_DISCUSSION = "discussion";
    private static final String POST_TYPE_SOLUTION = "solution";
    private static final String SOLUTION_MARKER = "__solution__";
    private static final String QUESTION_MARKER_PREFIX = "__question_";
    private static final String QUESTION_MARKER_SUFFIX = "__";

    @Resource
    private UserService userService;

    @Resource
    private PostThumbMapper postThumbMapper;

    @Resource
    private PostFavourMapper postFavourMapper;

    @Override
    public void validPost(Post post, boolean add) {
        if (post == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String title = post.getTitle();
        String content = post.getContent();
        String tags = post.getTags();
        if (add) {
            ThrowUtils.throwIf(StringUtils.isAnyBlank(title, content, tags), ErrorCode.PARAMS_ERROR);
        }
        if (StringUtils.isNotBlank(title) && title.length() > 80) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "标题过长");
        }
        if (StringUtils.isNotBlank(content) && content.length() > 8192) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "内容过长");
        }
    }

    @Override
    public QueryWrapper<Post> getQueryWrapper(PostQueryRequest postQueryRequest) {
        QueryWrapper<Post> queryWrapper = new QueryWrapper<>();
        if (postQueryRequest == null) {
            return queryWrapper;
        }
        String searchText = postQueryRequest.getSearchText();
        String sortField = postQueryRequest.getSortField();
        String sortOrder = postQueryRequest.getSortOrder();
        Long id = postQueryRequest.getId();
        String title = postQueryRequest.getTitle();
        String content = postQueryRequest.getContent();
        List<String> tagList = postQueryRequest.getTags();
        List<String> orTagList = postQueryRequest.getOrTags();
        Long userId = postQueryRequest.getUserId();
        Long notId = postQueryRequest.getNotId();
        String postType = postQueryRequest.getPostType();
        Long questionId = postQueryRequest.getQuestionId();
        // 拼接查询条件
        if (StringUtils.isNotBlank(searchText)) {
            queryWrapper.and(qw -> qw.like("title", searchText).or().like("content", searchText));
        }
        queryWrapper.like(StringUtils.isNotBlank(title), "title", title);
        queryWrapper.like(StringUtils.isNotBlank(content), "content", content);
        if (CollectionUtils.isNotEmpty(tagList)) {
            for (String tag : tagList) {
                queryWrapper.like("tags", "\"" + tag + "\"");
            }
        }
        if (CollectionUtils.isNotEmpty(orTagList)) {
            queryWrapper.and(qw -> {
                for (int i = 0; i < orTagList.size(); i++) {
                    String tag = orTagList.get(i);
                    if (i == 0) {
                        qw.like("tags", "\"" + tag + "\"");
                    } else {
                        qw.or().like("tags", "\"" + tag + "\"");
                    }
                }
            });
        }
        if (POST_TYPE_SOLUTION.equalsIgnoreCase(postType) || ObjectUtils.isNotEmpty(questionId)) {
            queryWrapper.like("tags", "\"" + SOLUTION_MARKER + "\"");
        } else if (POST_TYPE_DISCUSSION.equalsIgnoreCase(postType)) {
            queryWrapper.notLike("tags", "\"" + SOLUTION_MARKER + "\"");
        }
        if (ObjectUtils.isNotEmpty(questionId)) {
            queryWrapper.like("tags", "\"" + buildQuestionMarker(questionId) + "\"");
        }
        queryWrapper.ne(ObjectUtils.isNotEmpty(notId), "id", notId);
        queryWrapper.eq(ObjectUtils.isNotEmpty(id), "id", id);
        queryWrapper.eq(ObjectUtils.isNotEmpty(userId), "user_id", userId);
        boolean asc = CommonConstant.SORT_ORDER_ASC.equals(sortOrder);
        queryWrapper.orderBy(SqlUtils.validSortField(sortField), asc, sortField);
        return queryWrapper;
    }

    private String buildQuestionMarker(Long questionId) {
        return QUESTION_MARKER_PREFIX + questionId + QUESTION_MARKER_SUFFIX;
    }

    @Override
    public Page<Post> searchPost(PostQueryRequest postQueryRequest) {
        return this.page(new Page<>(postQueryRequest.getCurrent(), postQueryRequest.getPageSize()),
                this.getQueryWrapper(postQueryRequest));
    }

    @Override
    public PostVO getPostVO(Post post, HttpServletRequest request) {
        PostVO postVO = PostVO.objToVo(post);
        Long userId = post.getUserId();
        User user = null;
        if (userId != null && userId > 0) {
            user = userService.getById(userId);
        }
        UserVO userVO = userService.getUserVO(user);
        postVO.setUser(userVO);
        User loginUser = userService.getLoginUserPermitNull(request);
        if (loginUser != null) {
            com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<PostThumb> thumbQueryWrapper = new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>();
            thumbQueryWrapper.eq("post_id", post.getId());
            thumbQueryWrapper.eq("user_id", loginUser.getId());
            postVO.setHasThumb(postThumbMapper.selectCount(thumbQueryWrapper) > 0);

            com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<PostFavour> favourQueryWrapper = new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>();
            favourQueryWrapper.eq("post_id", post.getId());
            favourQueryWrapper.eq("user_id", loginUser.getId());
            postVO.setHasFavour(postFavourMapper.selectCount(favourQueryWrapper) > 0);
        }
        if (postVO.getHasThumb() == null) {
            postVO.setHasThumb(false);
        }
        if (postVO.getHasFavour() == null) {
            postVO.setHasFavour(false);
        }
        return postVO;
    }

    @Override
    public Page<PostVO> getPostVOPage(Page<Post> postPage, HttpServletRequest request) {
        List<Post> postList = postPage.getRecords();
        Page<PostVO> postVOPage = new Page<>(postPage.getCurrent(), postPage.getSize(), postPage.getTotal());
        if (CollectionUtils.isEmpty(postList)) {
            return postVOPage;
        }
        Set<Long> userIdSet = postList.stream().map(Post::getUserId).collect(Collectors.toSet());
        Map<Long, List<User>> userIdUserListMap = userService.listByIds(userIdSet).stream()
                .collect(Collectors.groupingBy(User::getId));

        User loginUser = userService.getLoginUserPermitNull(request);
        Set<Long> thumbPostIds = new HashSet<>();
        Set<Long> favourPostIds = new HashSet<>();
        if (loginUser != null) {
            Set<Long> postIdSet = postList.stream().map(Post::getId).collect(Collectors.toSet());
            if (CollectionUtils.isNotEmpty(postIdSet)) {
                com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<PostThumb> thumbQueryWrapper = new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>();
                thumbQueryWrapper.in("post_id", postIdSet);
                thumbQueryWrapper.eq("user_id", loginUser.getId());
                List<PostThumb> postThumbList = postThumbMapper.selectList(thumbQueryWrapper);
                thumbPostIds = postThumbList.stream().map(PostThumb::getPostId).collect(Collectors.toSet());

                com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<PostFavour> favourQueryWrapper = new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>();
                favourQueryWrapper.in("post_id", postIdSet);
                favourQueryWrapper.eq("user_id", loginUser.getId());
                List<PostFavour> postFavourList = postFavourMapper.selectList(favourQueryWrapper);
                favourPostIds = postFavourList.stream().map(PostFavour::getPostId).collect(Collectors.toSet());
            }
        }
        Set<Long> finalThumbPostIds = thumbPostIds;
        Set<Long> finalFavourPostIds = favourPostIds;
        List<PostVO> postVOList = postList.stream().map(post -> {
            PostVO postVO = PostVO.objToVo(post);
            Long userId = post.getUserId();
            User user = null;
            if (userIdUserListMap.containsKey(userId)) {
                user = userIdUserListMap.get(userId).get(0);
            }
            postVO.setUser(userService.getUserVO(user));
            postVO.setHasThumb(finalThumbPostIds.contains(post.getId()));
            postVO.setHasFavour(finalFavourPostIds.contains(post.getId()));
            return postVO;
        }).collect(Collectors.toList());
        postVOPage.setRecords(postVOList);
        return postVOPage;
    }
}
