package com.yupi.yuoj.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yupi.yuoj.common.ErrorCode;
import com.yupi.yuoj.constant.CommonConstant;
import com.yupi.yuoj.exception.BusinessException;
import com.yupi.yuoj.mapper.SolutionCommentMapper;
import com.yupi.yuoj.mapper.SolutionCommentThumbMapper;
import com.yupi.yuoj.model.dto.solutioncomment.SolutionCommentQueryRequest;
import com.yupi.yuoj.model.entity.SolutionComment;
import com.yupi.yuoj.model.entity.SolutionCommentThumb;
import com.yupi.yuoj.model.entity.User;
import com.yupi.yuoj.model.vo.SolutionCommentVO;
import com.yupi.yuoj.model.vo.UserVO;
import com.yupi.yuoj.service.SolutionCommentService;
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
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

/**
 * Solution comment service
 */
@Service
public class SolutionCommentServiceImpl extends ServiceImpl<SolutionCommentMapper, SolutionComment>
        implements SolutionCommentService {

    @Resource
    private UserService userService;

    @Resource
    private SolutionCommentThumbMapper solutionCommentThumbMapper;

    @Override
    public QueryWrapper<SolutionComment> getQueryWrapper(SolutionCommentQueryRequest request) {
        QueryWrapper<SolutionComment> queryWrapper = new QueryWrapper<>();
        if (request == null) {
            return queryWrapper;
        }
        Long id = request.getId();
        Long solutionId = request.getSolutionId();
        Long userId = request.getUserId();
        Long parentId = request.getParentId();
        Integer status = request.getStatus();
        String sortField = request.getSortField();
        String sortOrder = request.getSortOrder();
        queryWrapper.eq(ObjectUtils.isNotEmpty(id), "id", id);
        queryWrapper.eq(ObjectUtils.isNotEmpty(solutionId), "solution_id", solutionId);
        queryWrapper.eq(ObjectUtils.isNotEmpty(userId), "user_id", userId);
        queryWrapper.eq(ObjectUtils.isNotEmpty(parentId), "parent_id", parentId);
        queryWrapper.eq(ObjectUtils.isNotEmpty(status), "status", status);
        queryWrapper.eq("is_delete", 0);
        queryWrapper.orderBy(SqlUtils.validSortField(sortField), sortOrder.equals(CommonConstant.SORT_ORDER_ASC),
                sortField);
        return queryWrapper;
    }

    @Override
    public SolutionCommentVO getVO(SolutionComment comment, HttpServletRequest request) {
        SolutionCommentVO vo = SolutionCommentVO.objToVo(comment);
        if (vo == null) {
            return null;
        }
        Long userId = comment.getUserId();
        User user = null;
        if (userId != null && userId > 0) {
            user = userService.getById(userId);
        }
        UserVO userVO = userService.getUserVO(user);
        vo.setUserVO(userVO);
        User loginUser = userService.getLoginUserPermitNull(request);
        if (loginUser != null) {
            QueryWrapper<SolutionCommentThumb> thumbQueryWrapper = new QueryWrapper<>();
            thumbQueryWrapper.eq("comment_id", comment.getId());
            thumbQueryWrapper.eq("user_id", loginUser.getId());
            vo.setHasThumb(solutionCommentThumbMapper.selectCount(thumbQueryWrapper) > 0);
        }
        return vo;
    }

    @Override
    public Page<SolutionCommentVO> getVOPage(Page<SolutionComment> page, HttpServletRequest request) {
        List<SolutionComment> list = page.getRecords();
        Page<SolutionCommentVO> voPage = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        if (CollectionUtils.isEmpty(list)) {
            return voPage;
        }
        Set<Long> userIdSet = list.stream().map(SolutionComment::getUserId).collect(Collectors.toSet());
        Map<Long, List<User>> userMap = userService.listByIds(userIdSet).stream()
                .collect(Collectors.groupingBy(User::getId));
        
        // 填充点赞状态
        User loginUser = userService.getLoginUserPermitNull(request);
        Set<Long> thumbCommentIds = new HashSet<>();
        if (loginUser != null) {
            Set<Long> commentIds = list.stream().map(SolutionComment::getId).collect(Collectors.toSet());
            if (CollectionUtils.isNotEmpty(commentIds)) {
                QueryWrapper<SolutionCommentThumb> thumbQueryWrapper = new QueryWrapper<>();
                thumbQueryWrapper.in("comment_id", commentIds);
                thumbQueryWrapper.eq("user_id", loginUser.getId());
                List<SolutionCommentThumb> solutionCommentThumbs = solutionCommentThumbMapper.selectList(thumbQueryWrapper);
                thumbCommentIds = solutionCommentThumbs.stream().map(SolutionCommentThumb::getCommentId).collect(Collectors.toSet());
            }
        }
        Set<Long> finalThumbCommentIds = thumbCommentIds;

        List<SolutionCommentVO> voList = list.stream().map(item -> {
            SolutionCommentVO vo = SolutionCommentVO.objToVo(item);
            Long userId = item.getUserId();
            User user = null;
            if (userMap.containsKey(userId)) {
                user = userMap.get(userId).get(0);
            }
            vo.setUserVO(userService.getUserVO(user));
            vo.setHasThumb(finalThumbCommentIds.contains(item.getId()));
            return vo;
        }).collect(Collectors.toList());
        voPage.setRecords(voList);
        return voPage;
    }
}
