package com.yupi.yuoj.judge.codesandbox.impl;

import com.yupi.yuoj.judge.codesandbox.CodeSandbox;
import com.yupi.yuoj.judge.codesandbox.model.ExecuteCodeRequest;
import com.yupi.yuoj.judge.codesandbox.model.ExecuteCodeResponse;
import com.yupi.yuoj.judge.codesandbox.model.JudgeInfo;
import com.yupi.yuoj.model.enums.JudgeInfoMessageEnum;
import com.yupi.yuoj.model.enums.QuestionSubmitStatusEnum;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * 示例代码沙箱（仅为了跑通业务流程）
 */
@Slf4j
public class ExampleCodeSandbox implements CodeSandbox {
    @Override
    public ExecuteCodeResponse executeCode(ExecuteCodeRequest executeCodeRequest) {
        List<String> inputList = executeCodeRequest.getInputList();
        String code = executeCodeRequest.getCode();
        String language = executeCodeRequest.getLanguage();

        ExecuteCodeResponse executeCodeResponse = new ExecuteCodeResponse();
        executeCodeResponse.setMessage("测试执行成功");
        executeCodeResponse.setStatus(QuestionSubmitStatusEnum.SUCCEED.getValue());
        
        JudgeInfo judgeInfo = new JudgeInfo();
        judgeInfo.setMemory(100L);
        judgeInfo.setTime(100L);

        // 简单的 Mock：如果是 A+B 格式的输入，自动计算结果
        // 这样可以模拟 Accepted 的情况
        try {
            List<String> outputList = new java.util.ArrayList<>();
            for (String input : inputList) {
                String[] parts = input.trim().split(" ");
                if (parts.length == 2) {
                    int a = Integer.parseInt(parts[0]);
                    int b = Integer.parseInt(parts[1]);
                    outputList.add((a + b) + "");
                } else {
                    // 无法解析，原样返回（会导致 WA）
                    outputList.add(input);
                }
            }
            executeCodeResponse.setOutputList(outputList);
            judgeInfo.setMessage(JudgeInfoMessageEnum.ACCEPTED.getText());
        } catch (Exception e) {
            // 解析失败，模拟运行错误
            executeCodeResponse.setStatus(QuestionSubmitStatusEnum.FAILED.getValue());
            executeCodeResponse.setMessage(e.getMessage());
            judgeInfo.setMessage(JudgeInfoMessageEnum.RUNTIME_ERROR.getText());
        }

        executeCodeResponse.setJudgeInfo(judgeInfo);
        return executeCodeResponse;
    }
}
