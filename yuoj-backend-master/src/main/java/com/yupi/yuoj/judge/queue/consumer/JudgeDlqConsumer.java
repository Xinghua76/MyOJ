package com.yupi.yuoj.judge.queue.consumer;

import cn.hutool.json.JSONUtil;
import com.yupi.yuoj.judge.codesandbox.model.JudgeInfo;
import com.yupi.yuoj.judge.queue.model.JudgeTaskMessage;
import com.yupi.yuoj.model.entity.QuestionSubmit;
import com.yupi.yuoj.model.enums.JudgeInfoMessageEnum;
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
public class JudgeDlqConsumer {

    @Resource
    private QuestionSubmitService questionSubmitService;

    @RabbitListener(queues = "${judge.mq.dlq:code_arena.judge.dlq.v2}", containerFactory = "judgeRabbitListenerContainerFactory")
    public void handleDlq(JudgeTaskMessage message) {
        Long submitId = Optional.ofNullable(message)
                .map(JudgeTaskMessage::getQuestionSubmitId)
                .orElse(null);
        if (submitId == null || submitId <= 0) {
            log.warn("死信消息非法：" + String.valueOf(message));
            return;
        }
        QuestionSubmit submit = questionSubmitService.getById(submitId);
        if (submit == null) {
            log.warn("死信消息对应提交不存在：questionSubmitId=" + submitId);
            return;
        }
        if (!QuestionSubmitStatusEnum.WAITING.getValue().equals(submit.getStatus())
                && !QuestionSubmitStatusEnum.RUNNING.getValue().equals(submit.getStatus())) {
            log.info("死信消息忽略（状态不允许）：questionSubmitId=" + submitId + ", status=" + submit.getStatus());
            return;
        }
        JudgeInfo judgeInfo = new JudgeInfo();
        judgeInfo.setMessage(JudgeInfoMessageEnum.SYSTEM_ERROR.getValue());
        QuestionSubmit update = new QuestionSubmit();
        update.setId(submitId);
        update.setStatus(QuestionSubmitStatusEnum.FAILED.getValue());
        update.setJudgeInfo(JSONUtil.toJsonStr(judgeInfo));
        questionSubmitService.updateById(update);
        log.error("判题任务进入死信并标记失败：questionSubmitId=" + submitId);
    }
}
