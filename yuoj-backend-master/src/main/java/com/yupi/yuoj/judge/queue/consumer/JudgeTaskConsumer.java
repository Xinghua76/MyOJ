package com.yupi.yuoj.judge.queue.consumer;

import com.yupi.yuoj.judge.JudgeService;
import com.yupi.yuoj.judge.queue.model.JudgeTaskMessage;
import com.yupi.yuoj.model.entity.QuestionSubmit;
import com.yupi.yuoj.model.enums.QuestionSubmitStatusEnum;
import com.yupi.yuoj.service.QuestionSubmitService;
import java.util.Optional;
import javax.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@ConditionalOnProperty(prefix = "judge.worker", name = "enabled", havingValue = "true")
public class JudgeTaskConsumer {

    @Resource
    private QuestionSubmitService questionSubmitService;

    @Resource
    private JudgeService judgeService;

    @RabbitListener(queues = "${judge.mq.queue:code_arena.judge.queue.v2}", containerFactory = "judgeRabbitListenerContainerFactory")
    public void handleJudgeTask(JudgeTaskMessage message) {
        Long submitId = Optional.ofNullable(message)
                .map(JudgeTaskMessage::getQuestionSubmitId)
                .orElse(null);
        if (submitId == null || submitId <= 0) {
            log.warn("判题任务消息非法：" + String.valueOf(message));
            return;
        }
        log.info("收到判题任务：questionSubmitId=" + submitId);
        QuestionSubmit submit = questionSubmitService.getById(submitId);
        if (submit == null) {
            log.warn("判题任务对应提交不存在：questionSubmitId=" + submitId);
            return;
        }
        Integer status = submit.getStatus();
        if (status == null || !QuestionSubmitStatusEnum.WAITING.getValue().equals(status)) {
            log.info("判题任务忽略（非 WAITING）：questionSubmitId=" + submitId + ", status=" + status);
            return;
        }
        try {
            judgeService.doJudge(submitId);
            log.info("判题完成：questionSubmitId=" + submitId);
        } catch (RuntimeException e) {
            log.error("判题异常：questionSubmitId=" + submitId + ", message=" + e.getMessage(), e);
            QuestionSubmit latest = questionSubmitService.getById(submitId);
            if (latest != null && QuestionSubmitStatusEnum.RUNNING.getValue().equals(latest.getStatus())) {
                QuestionSubmit reset = new QuestionSubmit();
                reset.setId(submitId);
                reset.setStatus(QuestionSubmitStatusEnum.WAITING.getValue());
                questionSubmitService.updateById(reset);
            }
            throw e;
        }
    }
}
