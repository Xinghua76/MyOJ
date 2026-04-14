package com.yupi.yuoj.mapper;

import com.yupi.yuoj.model.entity.Question;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Update;

/**
 * a
 * @description 针对表【question(题目)】的数据库操作Mapper
 * @createDate 2026-03-07 20:58:00
 * @Entity com.yupi.yuoj.model.entity.Question
 */
public interface QuestionMapper extends BaseMapper<Question> {

    @Update("update question set acceptedNum = acceptedNum + 1 where id = #{id}")
    boolean incrementAcceptedNum(Long id);

}
