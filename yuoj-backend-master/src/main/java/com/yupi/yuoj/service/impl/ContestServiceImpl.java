package com.yupi.yuoj.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yupi.yuoj.constant.CommonConstant;
import com.yupi.yuoj.mapper.ContestMapper;
import com.yupi.yuoj.model.dto.contest.ContestQueryRequest;
import com.yupi.yuoj.model.entity.Contest;
import com.yupi.yuoj.model.entity.User;
import com.yupi.yuoj.model.vo.ContestVO;
import com.yupi.yuoj.model.vo.UserVO;
import com.yupi.yuoj.service.ContestService;
import com.yupi.yuoj.service.UserService;
import com.yupi.yuoj.utils.SqlUtils;
import java.util.Date;
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
 * Contest service
 */
@Service
public class ContestServiceImpl extends ServiceImpl<ContestMapper, Contest> implements ContestService {

    @Resource
    private UserService userService;

    @Override
    public QueryWrapper<Contest> getQueryWrapper(ContestQueryRequest request) {
        QueryWrapper<Contest> queryWrapper = new QueryWrapper<>();
        if (request == null) {
            return queryWrapper;
        }
        Long id = request.getId();
        String title = request.getTitle();
        Integer status = request.getStatus();
        Long creatorId = request.getCreatorId();
        String sortField = request.getSortField();
        String sortOrder = request.getSortOrder();
        queryWrapper.eq(ObjectUtils.isNotEmpty(id), "id", id);
        queryWrapper.like(StringUtils.isNotBlank(title), "title", title);
        
        // Dynamic status query based on time
        if (status != null) {
            Date now = new Date();
            if (status == 0) {
                // Not started: startTime > now
                queryWrapper.gt("start_time", now);
            } else if (status == 1) {
                // Running: startTime <= now AND endTime >= now
                queryWrapper.le("start_time", now);
                queryWrapper.ge("end_time", now);
            } else if (status == 2) {
                // Ended: endTime < now
                queryWrapper.lt("end_time", now);
            }
        }
        
        queryWrapper.eq(ObjectUtils.isNotEmpty(creatorId), "creator_id", creatorId);
        queryWrapper.eq("is_delete", 0);
        queryWrapper.orderBy(SqlUtils.validSortField(sortField), sortOrder.equals(CommonConstant.SORT_ORDER_ASC),
                sortField);
        return queryWrapper;
    }

    @Override
    public ContestVO getVO(Contest contest, HttpServletRequest request) {
        ContestVO vo = ContestVO.objToVo(contest);
        if (vo == null) {
            return null;
        }
        Long creatorId = contest.getCreatorId();
        User user = null;
        if (creatorId != null && creatorId > 0) {
            user = userService.getById(creatorId);
        }
        UserVO userVO = userService.getUserVO(user);
        vo.setCreatorVO(userVO);
        return vo;
    }

    @Override
    public Page<ContestVO> getVOPage(Page<Contest> page, HttpServletRequest request) {
        List<Contest> list = page.getRecords();
        Page<ContestVO> voPage = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        if (CollectionUtils.isEmpty(list)) {
            return voPage;
        }
        Set<Long> userIdSet = list.stream().map(Contest::getCreatorId).collect(Collectors.toSet());
        Map<Long, List<User>> userMap = userService.listByIds(userIdSet).stream()
                .collect(Collectors.groupingBy(User::getId));
        List<ContestVO> voList = list.stream().map(item -> {
            ContestVO vo = ContestVO.objToVo(item);
            Long uid = item.getCreatorId();
            User user = null;
            if (userMap.containsKey(uid)) {
                user = userMap.get(uid).get(0);
            }
            vo.setCreatorVO(userService.getUserVO(user));
            return vo;
        }).collect(Collectors.toList());
        voPage.setRecords(voList);
        return voPage;
    }
}
