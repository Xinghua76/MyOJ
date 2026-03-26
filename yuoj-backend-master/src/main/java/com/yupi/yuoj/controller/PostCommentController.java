package com.yupi.yuoj.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yupi.yuoj.annotation.AuthCheck;
import com.yupi.yuoj.common.BaseResponse;
import com.yupi.yuoj.common.DeleteRequest;
import com.yupi.yuoj.common.ErrorCode;
import com.yupi.yuoj.common.ResultUtils;
import com.yupi.yuoj.exception.BusinessException;
import com.yupi.yuoj.exception.ThrowUtils;
import com.yupi.yuoj.model.dto.postcomment.PostCommentAddRequest;
import com.yupi.yuoj.model.dto.postcomment.PostCommentQueryRequest;
import com.yupi.yuoj.model.entity.Post;
import com.yupi.yuoj.model.entity.PostComment;
import com.yupi.yuoj.model.entity.User;
import com.yupi.yuoj.model.vo.PostCommentVO;
import com.yupi.yuoj.service.PostCommentService;
import com.yupi.yuoj.service.PostCommentThumbService;
import com.yupi.yuoj.service.PostService;
import com.yupi.yuoj.service.UserService;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 帖子评论接口
 */
@RestController
@RequestMapping("/post_comment")
public class PostCommentController {

    @Resource
    private PostCommentService postCommentService;

    @Resource
    private UserService userService;

    @Resource
    private PostService postService;

    @Resource
    private PostCommentThumbService postCommentThumbService;

    @PostMapping("/add")
    public BaseResponse<Long> addPostComment(@RequestBody PostCommentAddRequest request, HttpServletRequest httpRequest) {
        if (request == null || request.getPostId() == null || request.getPostId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        if (StringUtils.isBlank(request.getContent())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "content required");
        }
        User loginUser = userService.getLoginUser(httpRequest);
        Post post = postService.getById(request.getPostId());
        ThrowUtils.throwIf(post == null, ErrorCode.NOT_FOUND_ERROR);

        if (request.getParentId() != null && request.getParentId() > 0) {
            PostComment parent = postCommentService.getById(request.getParentId());
            ThrowUtils.throwIf(parent == null, ErrorCode.NOT_FOUND_ERROR);
            if (!parent.getPostId().equals(request.getPostId())) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "parent not in same post");
            }
        }
        PostComment comment = new PostComment();
        BeanUtils.copyProperties(request, comment);
        comment.setUserId(loginUser.getId());
        comment.setThumbNum(0);
        comment.setStatus(1);
        boolean result = postCommentService.save(comment);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(comment.getId());
    }

    @PostMapping("/delete")
    public BaseResponse<Boolean> deletePostComment(@RequestBody DeleteRequest deleteRequest, HttpServletRequest httpRequest) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(httpRequest);
        PostComment old = postCommentService.getById(deleteRequest.getId());
        ThrowUtils.throwIf(old == null, ErrorCode.NOT_FOUND_ERROR);
        if (!old.getUserId().equals(loginUser.getId()) && !userService.isAdmin(loginUser)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        boolean result = postCommentService.removeById(deleteRequest.getId());
        return ResultUtils.success(result);
    }

    @PostMapping("/list/page/vo")
    public BaseResponse<Page<PostCommentVO>> listPostCommentVOByPage(@RequestBody PostCommentQueryRequest request,
            HttpServletRequest httpRequest) {
        long current = request.getCurrent();
        long size = request.getPageSize();
        Page<PostComment> page = postCommentService.page(new Page<>(current, size),
                postCommentService.getQueryWrapper(request));
        return ResultUtils.success(postCommentService.getPostCommentVOPage(page, httpRequest));
    }

    @PostMapping("/thumb")
    public BaseResponse<Integer> doThumb(@RequestBody PostCommentAddRequest request,
            HttpServletRequest httpRequest) {
        // 注意：这里复用 AddRequest 其实不太好，但为了方便传参暂且如此，或者用 DeleteRequest 传 id
        // 实际上点赞只需要 commentId，这里假设前端传的是 PostCommentAddRequest 里的 parentId 作为 commentId？
        // 不，应该定义专门的 ThumbRequest。这里暂时用 DeleteRequest 代替，或者从 addRequest 里取（如果不方便）。
        // 既然要规范，最好新建 Request。这里为了省事，我假设前端传 { id: commentId } 的 DeleteRequest 结构
        // 但前端代码里可能已经写好了调用方式。
        // 让我们看看 DeleteRequest: { id: number }
        // 所以用 DeleteRequest 是合适的。
        throw new BusinessException(ErrorCode.PARAMS_ERROR, "Invalid call");
    }

    @PostMapping("/do_thumb")
    public BaseResponse<Integer> doPostCommentThumb(@RequestBody DeleteRequest request,
            HttpServletRequest httpRequest) {
        if (request == null || request.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(httpRequest);
        long commentId = request.getId();
        int result = postCommentThumbService.doPostCommentThumb(commentId, loginUser);
        return ResultUtils.success(result);
    }
}