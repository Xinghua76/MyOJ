package com.yupi.yuoj.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yupi.yuoj.constant.CommonConstant;
import com.yupi.yuoj.mapper.NoticeMapper;
import com.yupi.yuoj.model.dto.notice.NoticeQueryRequest;
import com.yupi.yuoj.model.entity.Notice;
import com.yupi.yuoj.model.entity.User;
import com.yupi.yuoj.model.vo.NoticeVO;
import com.yupi.yuoj.model.vo.UserVO;
import com.yupi.yuoj.service.NoticeService;
import com.yupi.yuoj.service.UserService;
import com.yupi.yuoj.utils.SqlUtils;
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
 * Notice service
 */
@Service
public class NoticeServiceImpl extends ServiceImpl<NoticeMapper, Notice> implements NoticeService {

    @Resource
    private UserService userService;

    @Override
    public QueryWrapper<Notice> getQueryWrapper(NoticeQueryRequest request) {
        QueryWrapper<Notice> queryWrapper = new QueryWrapper<>();
        if (request == null) {
            return queryWrapper;
        }
        Long id = request.getId();
        String title = request.getTitle();
        Integer status = request.getStatus();
        Long publisherId = request.getPublisherId();
        String sortField = request.getSortField();
        String sortOrder = request.getSortOrder();
        queryWrapper.eq(ObjectUtils.isNotEmpty(id), "id", id);
        queryWrapper.like(StringUtils.isNotBlank(title), "title", title);
        queryWrapper.eq(ObjectUtils.isNotEmpty(status), "status", status);
        queryWrapper.eq(ObjectUtils.isNotEmpty(publisherId), "publisher_id", publisherId);
        queryWrapper.eq("is_delete", 0);
        queryWrapper.orderBy(SqlUtils.validSortField(sortField), sortOrder.equals(CommonConstant.SORT_ORDER_ASC),
                sortField);
        return queryWrapper;
    }

    @Override
    public NoticeVO getVO(Notice notice, HttpServletRequest request) {
        NoticeVO vo = NoticeVO.objToVo(notice);
        if (vo == null) {
            return null;
        }
        Long publisherId = notice.getPublisherId();
        User user = null;
        if (publisherId != null && publisherId > 0) {
            user = userService.getById(publisherId);
        }
        UserVO userVO = userService.getUserVO(user);
        vo.setPublisherVO(userVO);
        return vo;
    }

    @Override
    public Page<NoticeVO> getVOPage(Page<Notice> page, HttpServletRequest request) {
        List<Notice> list = page.getRecords();
        Page<NoticeVO> voPage = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        if (CollectionUtils.isEmpty(list)) {
            return voPage;
        }
        Set<Long> userIdSet = list.stream().map(Notice::getPublisherId).collect(Collectors.toSet());
        Map<Long, List<User>> userMap = userService.listByIds(userIdSet).stream()
                .collect(Collectors.groupingBy(User::getId));
        List<NoticeVO> voList = list.stream().map(item -> {
            NoticeVO vo = NoticeVO.objToVo(item);
            Long uid = item.getPublisherId();
            User user = null;
            if (userMap.containsKey(uid)) {
                user = userMap.get(uid).get(0);
            }
            vo.setPublisherVO(userService.getUserVO(user));
            return vo;
        }).collect(Collectors.toList());
        voPage.setRecords(voList);
        return voPage;
    }
}
