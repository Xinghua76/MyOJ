package com.yupi.yuoj.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.gson.Gson;
import com.yupi.yuoj.annotation.AuthCheck;
import com.yupi.yuoj.common.BaseResponse;
import com.yupi.yuoj.common.DeleteRequest;
import com.yupi.yuoj.common.ErrorCode;
import com.yupi.yuoj.common.ResultUtils;
import com.yupi.yuoj.constant.UserConstant;
import com.yupi.yuoj.exception.BusinessException;
import com.yupi.yuoj.exception.ThrowUtils;
import com.yupi.yuoj.model.dto.question.*;
import com.yupi.yuoj.model.dto.questionsubmit.QuestionSubmitAddRequest;
import com.yupi.yuoj.model.dto.questionsubmit.QuestionSubmitQueryRequest;
import com.yupi.yuoj.model.entity.Question;
import com.yupi.yuoj.model.entity.QuestionSubmit;
import com.yupi.yuoj.model.entity.User;
import com.yupi.yuoj.model.vo.QuestionSubmitVO;
import com.yupi.yuoj.model.vo.QuestionVO;
import com.yupi.yuoj.service.QuestionService;
import com.yupi.yuoj.service.QuestionSubmitService;
import com.yupi.yuoj.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

import com.alibaba.excel.EasyExcel;
import org.springframework.web.multipart.MultipartFile;
import java.util.ArrayList;
import java.util.stream.Collectors;
import com.yupi.yuoj.model.dto.question.QuestionImportExcelDTO;
import com.yupi.yuoj.model.dto.question.QuestionExportExcelDTO;
import com.yupi.yuoj.model.dto.question.DeleteBatchRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.URLEncoder;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * 题目接口
 *
 * @author <a href="https://github.com/liyupi">程序员鱼皮</a>
 * @from <a href="https://yupi.icu">编程导航知识星球</a>
 */
@RestController
@RequestMapping("/question")
@Slf4j
public class QuestionController {

    @Resource
    private QuestionService questionService;

    @Resource
    private UserService userService;

    @Resource
    private QuestionSubmitService questionSubmitService;

    private final static Gson GSON = new Gson();

    // region 增删改查

    /**
     * 创建
     *
     * @param questionAddRequest
     * @param request
     * @return
     */
    @PostMapping("/add")
    public BaseResponse<Long> addQuestion(@RequestBody QuestionAddRequest questionAddRequest,
            HttpServletRequest request) {
        if (questionAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Question question = new Question();
        BeanUtils.copyProperties(questionAddRequest, question);
        List<String> tags = questionAddRequest.getTags();
        if (tags != null) {
            question.setTags(GSON.toJson(tags));
        }
        List<JudgeCase> judgeCase = questionAddRequest.getJudgeCase();
        if (judgeCase != null) {
            question.setJudgeCase(GSON.toJson(judgeCase));
        }
        JudgeConfig judgeConfig = questionAddRequest.getJudgeConfig();
        if (judgeConfig != null) {
            question.setJudgeConfig(GSON.toJson(judgeConfig));
        }
        if (question.getDifficulty() == null) {
            question.setDifficulty(2);
        }
        if (question.getStatus() == null) {
            question.setStatus(1);
        }
        questionService.validQuestion(question, true);
        User loginUser = userService.getLoginUser(request);
        question.setUserId(loginUser.getId());
        question.setFavourNum(0);
        question.setThumbNum(0);
        boolean result = questionService.save(question);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        long newQuestionId = question.getId();
        return ResultUtils.success(newQuestionId);
    }

    /**
     * 删除
     *
     * @param deleteRequest
     * @param request
     * @return
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteQuestion(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = userService.getLoginUser(request);
        long id = deleteRequest.getId();
        // 判断是否存在
        Question oldQuestion = questionService.getById(id);
        ThrowUtils.throwIf(oldQuestion == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人或管理员可删除
        if (!oldQuestion.getUserId().equals(user.getId()) && !userService.isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        boolean b = questionService.removeById(id);
        return ResultUtils.success(b);
    }

    /**
     * 批量删除
     *
     * @param deleteBatchRequest
     * @param request
     * @return
     */
    @PostMapping("/delete/batch")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> deleteBatchQuestions(@RequestBody DeleteBatchRequest deleteBatchRequest,
            HttpServletRequest request) {
        if (deleteBatchRequest == null || CollectionUtils.isEmpty(deleteBatchRequest.getIds())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean b = questionService.removeByIds(deleteBatchRequest.getIds());
        return ResultUtils.success(b);
    }

    /**
     * 更新（仅管理员）
     *
     * @param questionUpdateRequest
     * @return
     */
    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateQuestion(@RequestBody QuestionUpdateRequest questionUpdateRequest) {
        if (questionUpdateRequest == null || questionUpdateRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Question question = new Question();
        BeanUtils.copyProperties(questionUpdateRequest, question);
        List<String> tags = questionUpdateRequest.getTags();
        if (tags != null) {
            question.setTags(GSON.toJson(tags));
        }
        List<JudgeCase> judgeCase = questionUpdateRequest.getJudgeCase();
        if (judgeCase != null) {
            question.setJudgeCase(GSON.toJson(judgeCase));
        }
        JudgeConfig judgeConfig = questionUpdateRequest.getJudgeConfig();
        if (judgeConfig != null) {
            question.setJudgeConfig(GSON.toJson(judgeConfig));
        }
        // 参数校验
        questionService.validQuestion(question, false);
        long id = questionUpdateRequest.getId();
        // 判断是否存在
        Question oldQuestion = questionService.getById(id);
        ThrowUtils.throwIf(oldQuestion == null, ErrorCode.NOT_FOUND_ERROR);
        boolean result = questionService.updateById(question);
        return ResultUtils.success(result);
    }

    /**
     * 根据 id 获取
     *
     * @param id
     * @return
     */
    @GetMapping("/get")
    public BaseResponse<Question> getQuestionById(long id, HttpServletRequest request) {
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Question question = questionService.getById(id);
        if (question == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        // 不是本人或管理员，不能直接获取所有信息
        if (!question.getUserId().equals(loginUser.getId()) && !userService.isAdmin(loginUser)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        return ResultUtils.success(question);
    }

    /**
     * 根据 id 获取（脱敏）
     *
     * @param id
     * @return
     */
    @GetMapping("/get/vo")
    public BaseResponse<QuestionVO> getQuestionVOById(long id, HttpServletRequest request) {
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Question question = questionService.getById(id);
        if (question == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        return ResultUtils.success(questionService.getQuestionVO(question, request));
    }

    /**
     * 分页获取列表（封装类）
     *
     * @param questionQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/list/page/vo")
    public BaseResponse<Page<QuestionVO>> listQuestionVOByPage(@RequestBody QuestionQueryRequest questionQueryRequest,
            HttpServletRequest request) {
        long current = questionQueryRequest.getCurrent();
        long size = questionQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        Page<Question> questionPage = questionService.page(new Page<>(current, size),
                questionService.getQueryWrapper(questionQueryRequest));
        return ResultUtils.success(questionService.getQuestionVOPage(questionPage, request));
    }

    /**
     * 分页获取当前用户创建的资源列表
     *
     * @param questionQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/my/list/page/vo")
    public BaseResponse<Page<QuestionVO>> listMyQuestionVOByPage(@RequestBody QuestionQueryRequest questionQueryRequest,
            HttpServletRequest request) {
        if (questionQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        questionQueryRequest.setUserId(loginUser.getId());
        long current = questionQueryRequest.getCurrent();
        long size = questionQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        Page<Question> questionPage = questionService.page(new Page<>(current, size),
                questionService.getQueryWrapper(questionQueryRequest));
        return ResultUtils.success(questionService.getQuestionVOPage(questionPage, request));
    }

    /**
     * 分页获取题目列表（仅管理员）
     *
     * @param questionQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/list/page")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<Question>> listQuestionByPage(@RequestBody QuestionQueryRequest questionQueryRequest,
            HttpServletRequest request) {
        long current = questionQueryRequest.getCurrent();
        long size = questionQueryRequest.getPageSize();
        Page<Question> questionPage = questionService.page(new Page<>(current, size),
                questionService.getQueryWrapper(questionQueryRequest));
        return ResultUtils.success(questionPage);
    }

    @PostMapping("/import")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> importQuestions(@RequestPart("file") MultipartFile file, HttpServletRequest request) {
        if (file == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        try {
            List<QuestionImportExcelDTO> list = EasyExcel.read(file.getInputStream())
                    .head(QuestionImportExcelDTO.class)
                    .sheet()
                    .doReadSync();
            if (list == null || list.isEmpty()) {
                return ResultUtils.success(true);
            }
            List<Question> questionList = list.stream().map(dto -> {
                Question question = new Question();
                question.setTitle(dto.getTitle());
                question.setContent(dto.getContent());
                if (dto.getTags() != null) {
                    question.setTags(GSON.toJson(dto.getTags().split(",")));
                } else {
                    question.setTags("[]");
                }
                question.setAnswer(dto.getAnswer());
                question.setJudgeCase(dto.getJudgeCase());
                question.setJudgeConfig(dto.getJudgeConfig());
                question.setUserId(loginUser.getId());
                question.setFavourNum(0);
                question.setThumbNum(0);
                question.setStatus(0);
                question.setDifficulty(1);
                return question;
            }).collect(Collectors.toList());
            boolean result = questionService.saveBatch(questionList);
            return ResultUtils.success(result);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "Import failed: " + e.getMessage());
        }
    }

    /**
     * 导出题目（仅管理员）
     *
     * @param questionQueryRequest 查询条件
     * @param response             响应对象
     */
    @GetMapping("/export")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public void exportQuestions(QuestionQueryRequest questionQueryRequest, HttpServletResponse response) {
        try {
            // 1. 设置响应头
            response.setContentType("application/vnd.ms-excel");
            response.setCharacterEncoding("utf-8");
            // 这里URLEncoder.encode可以防止中文乱码
            String fileName = URLEncoder.encode("题目数据", "UTF-8").replaceAll("\\+", "%20");
            response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");

            // 2. 查询数据（不分页，查询所有符合条件的数据）
            if (questionQueryRequest == null) {
                questionQueryRequest = new QuestionQueryRequest();
            }
            // 获取查询条件 Wrapper
            QueryWrapper<Question> queryWrapper = questionService.getQueryWrapper(questionQueryRequest);
            // 如果传入了 ids，则只导出选中的 ids
            if (CollectionUtils.isNotEmpty(questionQueryRequest.getIds())) {
                queryWrapper.in("id", questionQueryRequest.getIds());
            }
            List<Question> questionList = questionService.list(queryWrapper);

            if (CollectionUtils.isEmpty(questionList)) {
                // 如果没有数据，写入空 Excel 或直接返回
                EasyExcel.write(response.getOutputStream(), QuestionExportExcelDTO.class).sheet("题目列表")
                        .doWrite(new ArrayList<>());
                return;
            }

            // 3. 转换为导出对象
            List<QuestionExportExcelDTO> questionExportDTOList = questionList.stream().map(question -> {
                QuestionExportExcelDTO dto = new QuestionExportExcelDTO();
                BeanUtils.copyProperties(question, dto);
                // 处理标签：将 JSON 数组转换为逗号分隔的字符串
                String tagsJson = question.getTags();
                if (StringUtils.isNotBlank(tagsJson)) {
                    try {
                        List<String> tagList = GSON.fromJson(tagsJson, List.class);
                        if (CollectionUtils.isNotEmpty(tagList)) {
                            dto.setTags(String.join(",", tagList));
                        }
                    } catch (Exception e) {
                        log.error("解析标签失败, questionId = {}", question.getId());
                    }
                }
                return dto;
            }).collect(Collectors.toList());

            // 4. 写入 Excel
            EasyExcel.write(response.getOutputStream(), QuestionExportExcelDTO.class)
                    .sheet("题目列表")
                    .doWrite(questionExportDTOList);

        } catch (Exception e) {
            // 重置 response（如果还未写入）并抛出异常
            log.error("导出题目失败", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "导出失败");
        }
    }

    // endregion

    /**
     * 编辑（用户）
     *
     * @param questionEditRequest
     * @param request
     * @return
     */
    @PostMapping("/edit")
    public BaseResponse<Boolean> editQuestion(@RequestBody QuestionEditRequest questionEditRequest,
            HttpServletRequest request) {
        if (questionEditRequest == null || questionEditRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Question question = new Question();
        BeanUtils.copyProperties(questionEditRequest, question);
        List<String> tags = questionEditRequest.getTags();
        if (tags != null) {
            question.setTags(GSON.toJson(tags));
        }
        List<JudgeCase> judgeCase = questionEditRequest.getJudgeCase();
        if (judgeCase != null) {
            question.setJudgeCase(GSON.toJson(judgeCase));
        }
        JudgeConfig judgeConfig = questionEditRequest.getJudgeConfig();
        if (judgeConfig != null) {
            question.setJudgeConfig(GSON.toJson(judgeConfig));
        }
        // 参数校验
        questionService.validQuestion(question, false);
        User loginUser = userService.getLoginUser(request);
        long id = questionEditRequest.getId();
        // 判断是否存在
        Question oldQuestion = questionService.getById(id);
        ThrowUtils.throwIf(oldQuestion == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人或管理员可编辑
        if (!oldQuestion.getUserId().equals(loginUser.getId()) && !userService.isAdmin(loginUser)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        boolean result = questionService.updateById(question);
        return ResultUtils.success(result);
    }

    /**
     * 提交题目
     *
     * @param questionSubmitAddRequest
     * @param request
     * @return 提交记录的 id
     */
    @PostMapping("/question_submit/do")
    public BaseResponse<Long> doQuestionSubmit(@RequestBody QuestionSubmitAddRequest questionSubmitAddRequest,
            HttpServletRequest request) {
        if (questionSubmitAddRequest == null || questionSubmitAddRequest.getQuestionId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 登录才能点赞
        final User loginUser = userService.getLoginUser(request);
        long questionSubmitId = questionSubmitService.doQuestionSubmit(questionSubmitAddRequest, loginUser);
        return ResultUtils.success(questionSubmitId);
    }

    /**
     * 分页获取题目提交列表（除了管理员外，普通用户只能看到非答案、提交代码等公开信息）
     *
     * @param questionSubmitQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/question_submit/list/page")
    public BaseResponse<Page<QuestionSubmitVO>> listQuestionSubmitByPage(
            @RequestBody QuestionSubmitQueryRequest questionSubmitQueryRequest,
            HttpServletRequest request) {
        long current = questionSubmitQueryRequest.getCurrent();
        long size = questionSubmitQueryRequest.getPageSize();
        // 从数据库中查询原始的题目提交分页信息
        Page<QuestionSubmit> questionSubmitPage = questionSubmitService.page(new Page<>(current, size),
                questionSubmitService.getQueryWrapper(questionSubmitQueryRequest));
        final User loginUser = userService.getLoginUser(request);
        // 返回脱敏信息
        return ResultUtils.success(questionSubmitService.getQuestionSubmitVOPage(questionSubmitPage, loginUser));
    }

    /**
     * 根据 id 获取题目提交信息
     *
     * @param id
     * @return
     */
    @GetMapping("/question_submit/get")
    public BaseResponse<QuestionSubmitVO> getQuestionSubmitById(@RequestParam("id") long id,
            HttpServletRequest request) {
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        QuestionSubmit questionSubmit = questionSubmitService.getById(id);
        if (questionSubmit == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        final User loginUser = userService.getLoginUser(request);
        // 返回脱敏信息
        return ResultUtils.success(questionSubmitService.getQuestionSubmitVO(questionSubmit, loginUser));
    }

}
