package com.yupi.yuoj;

import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;

public class WorkerApplication {

    public static void main(String[] args) {
        new SpringApplicationBuilder(MainApplication.class)
                .profiles("worker")
                .web(WebApplicationType.NONE)
                .properties("judge.worker.enabled=true")
                .run(args);
    }
}
