package com.yupi.yuojcodesandbox;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import com.yupi.yuojcodesandbox.model.ExecuteCodeRequest;
import com.yupi.yuojcodesandbox.model.ExecuteCodeResponse;
import com.yupi.yuojcodesandbox.model.ExecuteMessage;
import com.yupi.yuojcodesandbox.model.JudgeInfo;
import com.yupi.yuojcodesandbox.utils.ProcessUtils;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Java 代码沙箱模板方法的实现
 */
@Slf4j
public abstract class JavaCodeSandboxTemplate implements CodeSandbox {

    private static final String GLOBAL_CODE_DIR_NAME = "tmpCode";

    private static final String GLOBAL_JAVA_CLASS_NAME = "Main.java";

    private static final long TIME_OUT = 5000L;

    @Override
    public ExecuteCodeResponse executeCode(ExecuteCodeRequest executeCodeRequest) {
        List<String> inputList = executeCodeRequest.getInputList();
        String code = executeCodeRequest.getCode();
        String language = executeCodeRequest.getLanguage();

        // 1. 把用户的代码保存为文件
        File userCodeFile = saveCodeToFile(code);

        // 2. 编译代码，得到 class 文件
        try {
            ExecuteMessage compileFileExecuteMessage = compileFile(userCodeFile);
            System.out.println(compileFileExecuteMessage);
        } catch (Exception e) {
            return getErrorResponse(e);
        }

        // 3. 执行代码，得到输出结果
        List<ExecuteMessage> executeMessageList = null;
        try {
            executeMessageList = runFile(userCodeFile, inputList);
        } catch (Exception e) {
            return getErrorResponse(e);
        }

        // 4. 收集整理输出结果
        ExecuteCodeResponse outputResponse = getOutputResponse(executeMessageList);

        // 5. 文件清理
        boolean b = deleteFile(userCodeFile);
        if (!b) {
            log.error("deleteFile error, userCodeFilePath = {}", userCodeFile.getAbsolutePath());
        }
        return outputResponse;
    }

    /**
     * 1. 把用户的代码保存为文件
     * 
     * @param code 用户代码
     * @return
     */
    public File saveCodeToFile(String code) {
        String userDir = System.getProperty("user.dir");
        String globalCodePathName = userDir + File.separator + GLOBAL_CODE_DIR_NAME;
        // 判断全局代码目录是否存在，没有则新建
        if (!FileUtil.exist(globalCodePathName)) {
            FileUtil.mkdir(globalCodePathName);
        }

        // 把用户的代码隔离存放
        String userCodeParentPath = globalCodePathName + File.separator + UUID.randomUUID();
        String userCodePath = userCodeParentPath + File.separator + GLOBAL_JAVA_CLASS_NAME;
        File userCodeFile = FileUtil.writeString(code, userCodePath, StandardCharsets.UTF_8);
        return userCodeFile;
    }

    /**
     * 2、编译代码
     * 
     * @param userCodeFile
     * @return
     */
    public ExecuteMessage compileFile(File userCodeFile) {
        // 使用 JavaCompiler API 进行编译，避免依赖 javac 命令
        javax.tools.JavaCompiler compiler = javax.tools.ToolProvider.getSystemJavaCompiler();
        ExecuteMessage executeMessage = new ExecuteMessage();

        if (compiler == null) {
            executeMessage.setExitValue(1);
            executeMessage.setErrorMessage("JDK not found or tools.jar missing");
            return executeMessage;
        }

        try (java.io.ByteArrayOutputStream errStream = new java.io.ByteArrayOutputStream()) {
            int exitCode = compiler.run(null, null, errStream,
                    "-encoding", "utf-8",
                    userCodeFile.getAbsolutePath());

            executeMessage.setExitValue(exitCode);
            if (exitCode != 0) {
                executeMessage.setErrorMessage(errStream.toString("UTF-8"));
                throw new RuntimeException("编译错误: " + executeMessage.getErrorMessage());
            }
            executeMessage.setMessage("编译成功");
            return executeMessage;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 3、执行文件，获得执行结果列表
     * 
     * @param userCodeFile
     * @param inputList
     * @return
     */
    public List<ExecuteMessage> runFile(File userCodeFile, List<String> inputList) {
        String userCodeParentPath = userCodeFile.getParentFile().getAbsolutePath();

        List<ExecuteMessage> executeMessageList = new ArrayList<>();
        for (String inputArgs : inputList) {
            try {
                String[] cmdArray = new String[] {
                        "java",
                        "-Xmx256m",
                        "-Dfile.encoding=UTF-8",
                        "-cp",
                        userCodeParentPath,
                        "Main"
                };
                Process runProcess = new ProcessBuilder(cmdArray).start();
                ExecuteMessage executeMessage = ProcessUtils.runInteractProcessAndGetMessage(runProcess, inputArgs,
                        TIME_OUT);
                System.out.println(executeMessage);
                executeMessageList.add(executeMessage);
            } catch (Exception e) {
                throw new RuntimeException("执行错误", e);
            }
        }
        return executeMessageList;
    }

    /**
     * 4、获取输出结果
     * 
     * @param executeMessageList
     * @return
     */
    public ExecuteCodeResponse getOutputResponse(List<ExecuteMessage> executeMessageList) {
        ExecuteCodeResponse executeCodeResponse = new ExecuteCodeResponse();
        List<String> outputList = new ArrayList<>();
        // 取用时最大值，便于判断是否超时
        long maxTime = 0;
        long maxMemory = 0;
        for (ExecuteMessage executeMessage : executeMessageList) {
            String errorMessage = executeMessage.getErrorMessage();
            if (StrUtil.isNotBlank(errorMessage)) {
                executeCodeResponse.setMessage(errorMessage);
                // 用户提交的代码执行中存在错误
                executeCodeResponse.setStatus(3);
                break;
            }
            outputList.add(StrUtil.nullToEmpty(executeMessage.getMessage()));
            Long time = executeMessage.getTime();
            if (time != null) {
                maxTime = Math.max(maxTime, time);
            }
            Long memory = executeMessage.getMemory();
            if (memory != null) {
                maxMemory = Math.max(maxMemory, memory);
            }
        }
        // 正常运行完成
        if (outputList.size() == executeMessageList.size()) {
            executeCodeResponse.setStatus(1);
        }
        executeCodeResponse.setOutputList(outputList);
        JudgeInfo judgeInfo = new JudgeInfo();
        judgeInfo.setTime(maxTime);
        if (maxMemory > 0) {
            judgeInfo.setMemory(maxMemory);
        }
        executeCodeResponse.setJudgeInfo(judgeInfo);
        return executeCodeResponse;
    }

    /**
     * 5、删除文件
     * 
     * @param userCodeFile
     * @return
     */
    public boolean deleteFile(File userCodeFile) {
        if (userCodeFile.getParentFile() != null) {
            String userCodeParentPath = userCodeFile.getParentFile().getAbsolutePath();
            boolean del = FileUtil.del(userCodeParentPath);
            System.out.println("删除" + (del ? "成功" : "失败"));
            return del;
        }
        return true;
    }

    /**
     * 6、获取错误响应
     *
     * @param e
     * @return
     */
    private ExecuteCodeResponse getErrorResponse(Throwable e) {
        ExecuteCodeResponse executeCodeResponse = new ExecuteCodeResponse();
        executeCodeResponse.setOutputList(new ArrayList<>());
        executeCodeResponse.setMessage(e.getMessage());
        // 表示代码沙箱错误
        executeCodeResponse.setStatus(2);
        executeCodeResponse.setJudgeInfo(new JudgeInfo());
        return executeCodeResponse;
    }
}
