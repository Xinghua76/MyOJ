package com.yupi.yuoj.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yupi.yuoj.model.dto.notice.NoticeQueryRequest;
import com.yupi.yuoj.model.entity.Notice;
import com.yupi.yuoj.model.vo.NoticeVO;
import javax.servlet.http.HttpServletRequest;

/**
 * Notice service
 */
public interface NoticeService extends IService<Notice> {

    QueryWrapper<Notice> getQueryWrapper(NoticeQueryRequest request);

    NoticeVO getVO(Notice notice, HttpServletRequest request);

    Page<NoticeVO> getVOPage(Page<Notice> page, HttpServletRequest request);
}
