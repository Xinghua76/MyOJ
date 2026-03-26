package com.yupi.yuoj.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yupi.yuoj.common.ErrorCode;
import com.yupi.yuoj.exception.BusinessException;
import com.yupi.yuoj.mapper.QuestionFavourMapper;
import com.yupi.yuoj.model.entity.Question;
import com.yupi.yuoj.model.entity.QuestionFavour;
import com.yupi.yuoj.model.entity.User;
import com.yupi.yuoj.service.QuestionFavourService;
import com.yupi.yuoj.service.QuestionService;
import javax.annotation.Resource;
import org.springframework.aop.framework.AopContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import java.util.List;

/**
 * 题目收藏服务实现
 */
@Service
public class QuestionFavourServiceImpl extends ServiceImpl<QuestionFavourMapper, QuestionFavour>
        implements QuestionFavourService {

    @Resource
    private QuestionService questionService;

    @Override
    public int doQuestionFavour(long questionId, User loginUser) {
        Question question = questionService.getById(questionId);
        if (question == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        long userId = loginUser.getId();
        // 每个用户串行题目收藏
        // 锁必须要包裹住事务，不然会出现在事务提交之前，锁释放了，其他线程获取锁，导致并发问题
        // 锁 userId，锁粒度较小
        QuestionFavourService questionFavourService = (QuestionFavourService) AopContext.currentProxy();
        synchronized (String.valueOf(userId).intern()) {
            return questionFavourService.doQuestionFavourInner(userId, questionId);
        }
    }

    @Override
    public Page<Question> listFavourQuestionByPage(Page<Question> page, QueryWrapper<Question> queryWrapper,
            long favourUserId) {
        if (favourUserId <= 0) {
            return new Page<>();
        }
        QueryWrapper<QuestionFavour> favourQueryWrapper = new QueryWrapper<>();
        favourQueryWrapper.select("question_id");
        favourQueryWrapper.eq("user_id", favourUserId);
        List<Object> questionIdList = this.listObjs(favourQueryWrapper);
        if (CollectionUtils.isEmpty(questionIdList)) {
            return new Page<>();
        }
        queryWrapper.in("id", questionIdList);
        return questionService.page(page, queryWrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int doQuestionFavourInner(long userId, long questionId) {
        QuestionFavour questionFavour = new QuestionFavour();
        questionFavour.setUserId(userId);
        questionFavour.setQuestionId(questionId);
        QueryWrapper<QuestionFavour> questionFavourQueryWrapper = new QueryWrapper<>();
        questionFavourQueryWrapper.eq("user_id", userId);
        questionFavourQueryWrapper.eq("question_id", questionId);
        QuestionFavour oldQuestionFavour = this.getOne(questionFavourQueryWrapper);
        boolean result;
        // 已收藏
        if (oldQuestionFavour != null) {
            result = this.remove(questionFavourQueryWrapper);
            if (result) {
                // 题目收藏数 - 1
                result = questionService.update().setSql("favour_num = favour_num - 1").eq("id", questionId)
                        .gt("favour_num", 0).update();
                return result ? -1 : 0;
            } else {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR);
            }
        } else {
            // 未收藏
            result = this.save(questionFavour);
            if (result) {
                // 题目收藏数 + 1
                result = questionService.update().setSql("favour_num = favour_num + 1").eq("id", questionId).update();
                return result ? 1 : 0;
            } else {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR);
            }
        }
    }
}
