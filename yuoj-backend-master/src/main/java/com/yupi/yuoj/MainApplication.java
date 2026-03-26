package com.yupi.yuoj;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.scheduling.annotation.EnableScheduling;


import java.util.TimeZone;
import javax.annotation.PostConstruct;

// todo 如需开启 Redis，须移除 exclude 中的内容
@SpringBootApplication
@MapperScan("com.yupi.yuoj.mapper")
@EnableScheduling
@EnableRabbit
@EnableAspectJAutoProxy(proxyTargetClass = true, exposeProxy = true)
public class MainApplication {

    public static void main(String[] args) {
        SpringApplication.run(MainApplication.class, args);
    }

    @PostConstruct
    void started() {
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Shanghai"));
    }

}
