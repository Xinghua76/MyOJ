package com.yupi.yuoj.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import com.yupi.yuoj.annotation.AuthCheck;
import com.yupi.yuoj.common.BaseResponse;
import com.yupi.yuoj.common.DeleteRequest;
import com.yupi.yuoj.common.ErrorCode;
import com.yupi.yuoj.common.ResultUtils;
import com.yupi.yuoj.constant.UserConstant;
import com.yupi.yuoj.exception.BusinessException;
import com.yupi.yuoj.exception.ThrowUtils;
import com.yupi.yuoj.model.dto.post.PostAddRequest;
import com.yupi.yuoj.model.dto.post.PostEditRequest;
import com.yupi.yuoj.model.dto.post.PostQueryRequest;
import com.yupi.yuoj.model.dto.post.PostUpdateRequest;
import com.yupi.yuoj.model.entity.Post;
import com.yupi.yuoj.model.entity.User;
import com.yupi.yuoj.model.vo.PostVO;
import com.yupi.yuoj.service.PostService;
import com.yupi.yuoj.service.UserService;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Solution endpoints (reusing /post)
 */
@RestController
@RequestMapping("/post")
@Slf4j
public class PostController {

    private static final Gson GSON = new Gson();
    private static final String POST_TYPE_DISCUSSION = "discussion";
    private static final String POST_TYPE_SOLUTION = "solution";
    private static final String SOLUTION_MARKER = "__solution__";
    private static final String QUESTION_MARKER_PREFIX = "__question_";
    private static final String QUESTION_MARKER_SUFFIX = "__";

    @Resource
    private PostService postService;

    @Resource
    private UserService userService;

    @PostMapping("/add")
    public BaseResponse<Long> addPost(@RequestBody PostAddRequest postAddRequest, HttpServletRequest request) {
        if (postAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Post post = new Post();
        BeanUtils.copyProperties(postAddRequest, post);
        List<String> tags = composeTags(postAddRequest.getTags(), postAddRequest.getPostType(),
                postAddRequest.getQuestionId(), null);
        post.setTags(GSON.toJson(tags));
        postService.validPost(post, true);
        User loginUser = userService.getLoginUser(request);
        post.setUserId(loginUser.getId());
        post.setThumbNum(0);
        post.setFavourNum(0);
        boolean result = postService.save(post);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        long newPostId = post.getId();
        return ResultUtils.success(newPostId);
    }

    @PostMapping("/delete")
    public BaseResponse<Boolean> deletePost(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = userService.getLoginUser(request);
        long id = deleteRequest.getId();
        Post oldPost = postService.getById(id);
        ThrowUtils.throwIf(oldPost == null, ErrorCode.NOT_FOUND_ERROR);
        if (!oldPost.getUserId().equals(user.getId()) && !userService.isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        boolean b = postService.removeById(id);
        return ResultUtils.success(b);
    }

    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updatePost(@RequestBody PostUpdateRequest postUpdateRequest) {
        if (postUpdateRequest == null || postUpdateRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Post post = new Post();
        BeanUtils.copyProperties(postUpdateRequest, post);
        List<String> tags = postUpdateRequest.getTags();
        if (tags != null) {
            post.setTags(GSON.toJson(tags));
        }
        // 参数校验
        postService.validPost(post, false);
        long id = postUpdateRequest.getId();
        // 判断是否存在
        Post oldPost = postService.getById(id);
        ThrowUtils.throwIf(oldPost == null, ErrorCode.NOT_FOUND_ERROR);
        boolean result = postService.updateById(post);
        return ResultUtils.success(result);
    }

    @GetMapping("/get/vo")
    public BaseResponse<PostVO> getPostVOById(long id, HttpServletRequest request) {
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Post post = postService.getById(id);
        if (post == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        return ResultUtils.success(postService.getPostVO(post, request));
    }

    @PostMapping("/list/page/vo")
    public BaseResponse<Page<PostVO>> listPostVOByPage(@RequestBody PostQueryRequest postQueryRequest,
            HttpServletRequest request) {
        long current = postQueryRequest.getCurrent();
        long size = postQueryRequest.getPageSize();
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        Page<Post> postPage = postService.page(new Page<>(current, size),
                postService.getQueryWrapper(postQueryRequest));
        return ResultUtils.success(postService.getPostVOPage(postPage, request));
    }

    @PostMapping("/my/list/page/vo")
    public BaseResponse<Page<PostVO>> listMyPostVOByPage(@RequestBody PostQueryRequest postQueryRequest,
            HttpServletRequest request) {
        if (postQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        postQueryRequest.setUserId(loginUser.getId());
        long current = postQueryRequest.getCurrent();
        long size = postQueryRequest.getPageSize();
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        Page<Post> postPage = postService.page(new Page<>(current, size),
                postService.getQueryWrapper(postQueryRequest));
        return ResultUtils.success(postService.getPostVOPage(postPage, request));
    }

    @PostMapping("/search/page/vo")
    public BaseResponse<Page<PostVO>> searchPostVOByPage(@RequestBody PostQueryRequest postQueryRequest,
            HttpServletRequest request) {
        long size = postQueryRequest.getPageSize();
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        Page<Post> postPage = postService.searchPost(postQueryRequest);
        return ResultUtils.success(postService.getPostVOPage(postPage, request));
    }

    @PostMapping("/edit")
    public BaseResponse<Boolean> editPost(@RequestBody PostEditRequest postEditRequest, HttpServletRequest request) {
        if (postEditRequest == null || postEditRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        long id = postEditRequest.getId();
        Post oldPost = postService.getById(id);
        ThrowUtils.throwIf(oldPost == null, ErrorCode.NOT_FOUND_ERROR);

        Post post = new Post();
        BeanUtils.copyProperties(postEditRequest, post);
        List<String> tags = postEditRequest.getTags();
        if (tags != null || StringUtils.isNotBlank(postEditRequest.getPostType())
                || postEditRequest.getQuestionId() != null) {
            List<String> finalTags = composeTags(tags, postEditRequest.getPostType(), postEditRequest.getQuestionId(), oldPost);
            post.setTags(GSON.toJson(finalTags));
        }
        // 参数校验
        postService.validPost(post, false);
        User loginUser = userService.getLoginUser(request);
        // 仅本人或管理员可编辑
        if (!oldPost.getUserId().equals(loginUser.getId()) && !userService.isAdmin(loginUser)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        boolean result = postService.updateById(post);
        return ResultUtils.success(result);
    }

    private List<String> composeTags(List<String> inputTags, String postType, Long questionId, Post oldPost) {
        Set<String> tagSet = new HashSet<>();
        if (inputTags != null) {
            for (String tag : inputTags) {
                if (StringUtils.isNotBlank(tag) && !isInternalMarker(tag)) {
                    tagSet.add(tag.trim());
                }
            }
        }

        boolean oldIsSolution = false;
        Long oldQuestionId = null;
        if (oldPost != null && StringUtils.isNotBlank(oldPost.getTags())) {
            List<String> oldTags = GSON.fromJson(oldPost.getTags(), new TypeToken<List<String>>() {
            }.getType());
            if (oldTags != null) {
                oldIsSolution = oldTags.contains(SOLUTION_MARKER);
                for (String oldTag : oldTags) {
                    Long parsed = parseQuestionIdFromMarker(oldTag);
                    if (parsed != null) {
                        oldQuestionId = parsed;
                        break;
                    }
                }
            }
        }

        String normalizedType = StringUtils.isBlank(postType) ? null : postType.trim().toLowerCase();
        boolean isSolution = POST_TYPE_SOLUTION.equals(normalizedType)
                || (normalizedType == null && oldIsSolution);

        if (POST_TYPE_DISCUSSION.equals(normalizedType)) {
            isSolution = false;
        }

        if (isSolution) {
            Long finalQuestionId = questionId != null ? questionId : oldQuestionId;
            if (finalQuestionId == null || finalQuestionId <= 0) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "题解必须关联题目");
            }
            tagSet.add(SOLUTION_MARKER);
            tagSet.add(buildQuestionMarker(finalQuestionId));
        }

        return new ArrayList<>(tagSet);
    }

    private boolean isInternalMarker(String tag) {
        if (StringUtils.isBlank(tag)) {
            return false;
        }
        return SOLUTION_MARKER.equals(tag) || tag.startsWith(QUESTION_MARKER_PREFIX);
    }

    private String buildQuestionMarker(Long questionId) {
        return QUESTION_MARKER_PREFIX + questionId + QUESTION_MARKER_SUFFIX;
    }

    private Long parseQuestionIdFromMarker(String tag) {
        if (StringUtils.isBlank(tag) || !tag.startsWith(QUESTION_MARKER_PREFIX)
                || !tag.endsWith(QUESTION_MARKER_SUFFIX)) {
            return null;
        }
        String number = tag.substring(QUESTION_MARKER_PREFIX.length(), tag.length() - QUESTION_MARKER_SUFFIX.length());
        if (!StringUtils.isNumeric(number)) {
            return null;
        }
        return Long.parseLong(number);
    }
}
