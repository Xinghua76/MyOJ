package com.yupi.yuoj.judge.queue.producer;

import com.yupi.yuoj.judge.queue.model.JudgeTaskMessage;
import javax.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class JudgeTaskProducer {

    @Resource
    private RabbitTemplate rabbitTemplate;

    @Value("${judge.mq.exchange:code_arena.judge.exchange}")
    private String exchangeName;

    @Value("${judge.mq.routing-key:code_arena.judge}")
    private String routingKey;

    public void sendJudgeTask(long questionSubmitId) {
        JudgeTaskMessage message = JudgeTaskMessage.builder()
                .questionSubmitId(questionSubmitId)
                .build();
        log.info("判题任务准备投递：questionSubmitId=" + questionSubmitId);
        rabbitTemplate.convertAndSend(exchangeName, routingKey, message);
        log.info("判题任务投递成功：questionSubmitId=" + questionSubmitId);
    }
}
