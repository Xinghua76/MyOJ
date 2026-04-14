package com.yupi.yuoj.judge.codesandbox.model;

import lombok.Data;

/**
 * 判题信息
 */
@Data
public class JudgeInfo {

    /**
     * 程序执行信息
     */
    private String message;

    /**
     * 消耗内存
     */
    private Long memory;

    /**
     * 消耗时间（KB）
     */
    private Long time;

    /**
     * 第一个失败用例的输入
     */
    private String input;

    /**
     * 第一个失败用例的期望输出
     */
    private String expectedOutput;

    /**
     * 第一个失败用例的实际输出
     */
    private String actualOutput;
}
