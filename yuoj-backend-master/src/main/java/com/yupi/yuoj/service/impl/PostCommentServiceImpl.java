package com.yupi.yuoj.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yupi.yuoj.constant.CommonConstant;
import com.yupi.yuoj.mapper.PostCommentMapper;
import com.yupi.yuoj.mapper.PostCommentThumbMapper;
import com.yupi.yuoj.model.dto.postcomment.PostCommentQueryRequest;
import com.yupi.yuoj.model.entity.PostComment;
import com.yupi.yuoj.model.entity.PostCommentThumb;
import com.yupi.yuoj.model.entity.User;
import com.yupi.yuoj.model.vo.PostCommentVO;
import com.yupi.yuoj.model.vo.UserVO;
import com.yupi.yuoj.service.PostCommentService;
import com.yupi.yuoj.service.UserService;
import com.yupi.yuoj.utils.SqlUtils;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Service;

/**
 * 帖子评论服务实现
 */
@Service
public class PostCommentServiceImpl extends ServiceImpl<PostCommentMapper, PostComment>
        implements PostCommentService {

    @Resource
    private UserService userService;

    @Resource
    private PostCommentThumbMapper postCommentThumbMapper;

    @Override
    public QueryWrapper<PostComment> getQueryWrapper(PostCommentQueryRequest request) {
        QueryWrapper<PostComment> queryWrapper = new QueryWrapper<>();
        if (request == null) {
            return queryWrapper;
        }
        Long id = request.getId();
        Long postId = request.getPostId();
        Long userId = request.getUserId();
        Long parentId = request.getParentId();
        Integer status = request.getStatus();
        String sortField = request.getSortField();
        String sortOrder = request.getSortOrder();
        queryWrapper.eq(ObjectUtils.isNotEmpty(id), "id", id);
        queryWrapper.eq(ObjectUtils.isNotEmpty(postId), "post_id", postId);
        queryWrapper.eq(ObjectUtils.isNotEmpty(userId), "user_id", userId);
        queryWrapper.eq(ObjectUtils.isNotEmpty(parentId), "parent_id", parentId);
        queryWrapper.eq(ObjectUtils.isNotEmpty(status), "status", status);
        queryWrapper.eq("is_delete", 0);
        queryWrapper.orderBy(SqlUtils.validSortField(sortField), sortOrder.equals(CommonConstant.SORT_ORDER_ASC),
                sortField);
        return queryWrapper;
    }

    @Override
    public PostCommentVO getPostCommentVO(PostComment postComment, HttpServletRequest request) {
        PostCommentVO vo = PostCommentVO.objToVo(postComment);
        if (vo == null) {
            return null;
        }
        Long userId = postComment.getUserId();
        User user = null;
        if (userId != null && userId > 0) {
            user = userService.getById(userId);
        }
        UserVO userVO = userService.getUserVO(user);
        vo.setUser(userVO);
        User loginUser = userService.getLoginUserPermitNull(request);
        if (loginUser != null) {
            QueryWrapper<PostCommentThumb> thumbQueryWrapper = new QueryWrapper<>();
            thumbQueryWrapper.eq("comment_id", postComment.getId());
            thumbQueryWrapper.eq("user_id", loginUser.getId());
            vo.setHasThumb(postCommentThumbMapper.selectCount(thumbQueryWrapper) > 0);
        }
        return vo;
    }

    @Override
    public Page<PostCommentVO> getPostCommentVOPage(Page<PostComment> page, HttpServletRequest request) {
        List<PostComment> list = page.getRecords();
        Page<PostCommentVO> voPage = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        if (CollectionUtils.isEmpty(list)) {
            return voPage;
        }
        Set<Long> userIdSet = list.stream().map(PostComment::getUserId).collect(Collectors.toSet());
        Map<Long, List<User>> userMap = userService.listByIds(userIdSet).stream()
                .collect(Collectors.groupingBy(User::getId));

        // 填充点赞状态
        User loginUser = userService.getLoginUserPermitNull(request);
        Set<Long> thumbCommentIds = new HashSet<>();
        if (loginUser != null) {
            Set<Long> commentIds = list.stream().map(PostComment::getId).collect(Collectors.toSet());
            if (CollectionUtils.isNotEmpty(commentIds)) {
                QueryWrapper<PostCommentThumb> thumbQueryWrapper = new QueryWrapper<>();
                thumbQueryWrapper.in("comment_id", commentIds);
                thumbQueryWrapper.eq("user_id", loginUser.getId());
                List<PostCommentThumb> postCommentThumbs = postCommentThumbMapper.selectList(thumbQueryWrapper);
                thumbCommentIds = postCommentThumbs.stream().map(PostCommentThumb::getCommentId).collect(Collectors.toSet());
            }
        }
        Set<Long> finalThumbCommentIds = thumbCommentIds;

        List<PostCommentVO> voList = list.stream().map(item -> {
            PostCommentVO vo = PostCommentVO.objToVo(item);
            Long userId = item.getUserId();
            User user = null;
            if (userMap.containsKey(userId)) {
                user = userMap.get(userId).get(0);
            }
            vo.setUser(userService.getUserVO(user));
            vo.setHasThumb(finalThumbCommentIds.contains(item.getId()));
            return vo;
        }).collect(Collectors.toList());
        voPage.setRecords(voList);
        return voPage;
    }
}