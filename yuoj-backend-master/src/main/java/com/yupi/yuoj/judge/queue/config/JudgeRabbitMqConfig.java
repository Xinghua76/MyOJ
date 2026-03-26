package com.yupi.yuoj.judge.queue.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.Map;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.config.RetryInterceptorBuilder;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.listener.ConditionalRejectingErrorHandler;
import org.springframework.amqp.rabbit.retry.RejectAndDontRequeueRecoverer;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.interceptor.RetryOperationsInterceptor;

@Configuration
public class JudgeRabbitMqConfig {

    @Value("${judge.mq.exchange:code_arena.judge.exchange}")
    private String exchangeName;

    @Value("${judge.mq.queue:code_arena.judge.queue.v2}")
    private String queueName;

    @Value("${judge.mq.routing-key:code_arena.judge}")
    private String routingKey;

    @Value("${judge.mq.dlx.exchange:code_arena.judge.dlx.exchange}")
    private String dlxExchangeName;

    @Value("${judge.mq.dlq:code_arena.judge.dlq.v2}")
    private String dlqName;

    @Value("${judge.mq.dlx.routing-key:code_arena.judge.dlx}")
    private String dlxRoutingKey;

    @Bean
    public DirectExchange judgeExchange() {
        return new DirectExchange(exchangeName, true, false);
    }

    @Bean
    public DirectExchange judgeDlxExchange() {
        return new DirectExchange(dlxExchangeName, true, false);
    }

    @Bean
    public Queue judgeQueue() {
        Map<String, Object> args = new HashMap<>();
        args.put("x-dead-letter-exchange", dlxExchangeName);
        args.put("x-dead-letter-routing-key", dlxRoutingKey);
        return QueueBuilder.durable(queueName).withArguments(args).build();
    }

    @Bean
    public Queue judgeDlq() {
        return QueueBuilder.durable(dlqName).build();
    }

    @Bean
    public Binding judgeBinding(Queue judgeQueue, DirectExchange judgeExchange) {
        return BindingBuilder.bind(judgeQueue).to(judgeExchange).with(routingKey);
    }

    @Bean
    public Binding judgeDlqBinding(Queue judgeDlq, DirectExchange judgeDlxExchange) {
        return BindingBuilder.bind(judgeDlq).to(judgeDlxExchange).with(dlxRoutingKey);
    }

    @Bean
    public MessageConverter messageConverter(ObjectMapper objectMapper) {
        return new Jackson2JsonMessageConverter(objectMapper);
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, MessageConverter messageConverter) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(messageConverter);
        return rabbitTemplate;
    }

    @Bean
    public RetryOperationsInterceptor judgeRetryInterceptor(
            @Value("${judge.mq.retry.max-attempts:3}") int maxAttempts
    ) {
        return RetryInterceptorBuilder
                .stateless()
                .maxAttempts(maxAttempts)
                .recoverer(new RejectAndDontRequeueRecoverer())
                .build();
    }

    @Bean
    public SimpleRabbitListenerContainerFactory judgeRabbitListenerContainerFactory(
            ConnectionFactory connectionFactory,
            MessageConverter messageConverter,
            RetryOperationsInterceptor judgeRetryInterceptor
    ) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(messageConverter);
        factory.setAdviceChain(judgeRetryInterceptor);
        factory.setErrorHandler(new ConditionalRejectingErrorHandler());
        factory.setDefaultRequeueRejected(false);
        factory.setPrefetchCount(1);
        return factory;
    }
}
