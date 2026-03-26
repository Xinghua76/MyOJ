package com.yupi.yuojcodesandbox.utils;

import cn.hutool.core.util.StrUtil;
import com.yupi.yuojcodesandbox.model.ExecuteMessage;
import org.springframework.util.StopWatch;

import java.io.*;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 进程工具类
 */
public class ProcessUtils {

    private static final Pattern PROC_STATUS_VM_HWM_PATTERN = Pattern.compile("^VmHWM:\\s+(\\d+)\\s+kB\\s*$");
    private static final Pattern PROC_STATUS_VM_RSS_PATTERN = Pattern.compile("^VmRSS:\\s+(\\d+)\\s+kB\\s*$");

    /**
     * 执行进程并获取信息
     *
     * @param runProcess
     * @param opName
     * @return
     */
    public static ExecuteMessage runProcessAndGetMessage(Process runProcess, String opName) {
        ExecuteMessage executeMessage = new ExecuteMessage();

        AtomicBoolean sampling = new AtomicBoolean(true);
        AtomicLong maxMemoryKb = new AtomicLong(0);
        Thread memorySampler = startProcStatusMemorySampler(runProcess, sampling, maxMemoryKb);
        File procStatusFile = getProcStatusFileIfPresent(runProcess);

        try {
            StopWatch stopWatch = new StopWatch();
            stopWatch.start();

            // 使用 StringBuilder 收集输出，避免死锁
            StringBuilder outputBuilder = new StringBuilder();
            StringBuilder errorBuilder = new StringBuilder();

            // 启动线程读取 stdout
            Thread outputThread = new Thread(() -> {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(runProcess.getInputStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        outputBuilder.append(line).append("\n");
                    }
                } catch (IOException e) {
                    // ignore
                }
            });

            // 启动线程读取 stderr
            Thread errorThread = new Thread(() -> {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(runProcess.getErrorStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        errorBuilder.append(line).append("\n");
                    }
                } catch (IOException e) {
                    // ignore
                }
            });

            outputThread.start();
            errorThread.start();

            // 等待程序执行，获取错误码
            if (procStatusFile != null) {
                updateMaxMemoryKb(procStatusFile, maxMemoryKb);
            }
            int exitValue = runProcess.waitFor();
            if (procStatusFile != null) {
                updateMaxMemoryKb(procStatusFile, maxMemoryKb);
            }

            // 确保读取线程结束
            try {
                outputThread.join();
                errorThread.join();
            } catch (InterruptedException e) {
                // ignore
            }

            // readLine 会丢掉换行符，这里用 append("\n") 手动拼接，会导致末尾多一个 \n，需去掉
            if (outputBuilder.length() > 0 && outputBuilder.charAt(outputBuilder.length() - 1) == '\n') {
                outputBuilder.setLength(outputBuilder.length() - 1);
            }
            if (errorBuilder.length() > 0 && errorBuilder.charAt(errorBuilder.length() - 1) == '\n') {
                errorBuilder.setLength(errorBuilder.length() - 1);
            }

            executeMessage.setExitValue(exitValue);
            if (exitValue == 0) {
                System.out.println(opName + "成功");
                executeMessage.setMessage(outputBuilder.toString());
            } else {
                System.out.println(opName + "失败，错误码： " + exitValue);
                executeMessage.setMessage(outputBuilder.toString());
                executeMessage.setErrorMessage(errorBuilder.toString());
            }
            stopWatch.stop();
            executeMessage.setTime(stopWatch.getLastTaskTimeMillis());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            sampling.set(false);
            if (memorySampler != null) {
                try {
                    memorySampler.join(200);
                } catch (InterruptedException e) {
                    // ignore
                }
            }
            long memoryKb = maxMemoryKb.get();
            if (memoryKb > 0) {
                executeMessage.setMemory(memoryKb);
            }
        }
        return executeMessage;
    }

    /**
     * 执行交互式进程并获取信息
     *
     * @param runProcess
     * @param args
     * @return
     */
    public static ExecuteMessage runInteractProcessAndGetMessage(Process runProcess, String args) {
        ExecuteMessage executeMessage = new ExecuteMessage();

        try {
            // 向控制台输入程序
            OutputStream outputStream = runProcess.getOutputStream();
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(outputStream);
            String[] s = args.split(" ");
            String join = StrUtil.join("\n", s) + "\n";
            outputStreamWriter.write(join);
            // 相当于按了回车，执行输入的发送
            outputStreamWriter.flush();

            // 分批获取进程的正常输出
            InputStream inputStream = runProcess.getInputStream();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder compileOutputStringBuilder = new StringBuilder();
            // 逐行读取
            String compileOutputLine;
            while ((compileOutputLine = bufferedReader.readLine()) != null) {
                compileOutputStringBuilder.append(compileOutputLine);
            }
            executeMessage.setMessage(compileOutputStringBuilder.toString());
            // 记得资源的释放，否则会卡死
            outputStreamWriter.close();
            outputStream.close();
            inputStream.close();
            runProcess.destroy();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return executeMessage;
    }

    public static ExecuteMessage runInteractProcessAndGetMessage(Process runProcess, String input, long timeoutMs) {
        ExecuteMessage executeMessage = new ExecuteMessage();

        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        AtomicBoolean sampling = new AtomicBoolean(true);
        AtomicLong maxMemoryKb = new AtomicLong(0);
        Thread memorySampler = startProcStatusMemorySampler(runProcess, sampling, maxMemoryKb);
        File procStatusFile = getProcStatusFileIfPresent(runProcess);

        StringBuilder outputBuilder = new StringBuilder();
        StringBuilder errorBuilder = new StringBuilder();

        Thread outputThread = new Thread(() -> {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(runProcess.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    outputBuilder.append(line).append("\n");
                }
            } catch (IOException e) {
                // ignore
            }
        });

        Thread errorThread = new Thread(() -> {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(runProcess.getErrorStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    errorBuilder.append(line).append("\n");
                }
            } catch (IOException e) {
                // ignore
            }
        });

        outputThread.start();
        errorThread.start();

        try {
            if (input != null) {
                try (OutputStream outputStream = runProcess.getOutputStream();
                        OutputStreamWriter outputStreamWriter = new OutputStreamWriter(outputStream)) {
                    outputStreamWriter.write(input);
                    if (!input.endsWith("\n")) {
                        outputStreamWriter.write("\n");
                    }
                    outputStreamWriter.flush();
                }
            } else {
                try {
                    runProcess.getOutputStream().close();
                } catch (IOException e) {
                    // ignore
                }
            }

            if (procStatusFile != null) {
                updateMaxMemoryKb(procStatusFile, maxMemoryKb);
            }
            boolean finished = runProcess.waitFor(timeoutMs, TimeUnit.MILLISECONDS);
            if (procStatusFile != null) {
                updateMaxMemoryKb(procStatusFile, maxMemoryKb);
            }
            if (!finished) {
                System.out.println("超时了，中断");
                runProcess.destroyForcibly();
                runProcess.waitFor();
                executeMessage.setExitValue(143);
                executeMessage.setErrorMessage("Time Limit Exceeded");
            } else {
                executeMessage.setExitValue(runProcess.exitValue());
            }
        } catch (Exception e) {
            executeMessage.setExitValue(1);
            executeMessage.setErrorMessage(e.getMessage());
        } finally {
            sampling.set(false);
            if (memorySampler != null) {
                try {
                    memorySampler.join(200);
                } catch (InterruptedException e) {
                    // ignore
                }
            }
            try {
                outputThread.join(1000);
                errorThread.join(1000);
            } catch (InterruptedException e) {
                // ignore
            }
        }

        if (outputBuilder.length() > 0 && outputBuilder.charAt(outputBuilder.length() - 1) == '\n') {
            outputBuilder.setLength(outputBuilder.length() - 1);
        }
        if (errorBuilder.length() > 0 && errorBuilder.charAt(errorBuilder.length() - 1) == '\n') {
            errorBuilder.setLength(errorBuilder.length() - 1);
        }

        executeMessage.setMessage(outputBuilder.toString());
        if (executeMessage.getErrorMessage() == null || executeMessage.getErrorMessage().isEmpty()) {
            executeMessage.setErrorMessage(errorBuilder.toString());
        }

        stopWatch.stop();
        executeMessage.setTime(stopWatch.getLastTaskTimeMillis());
        long memoryKb = maxMemoryKb.get();
        if (memoryKb > 0) {
            executeMessage.setMemory(memoryKb);
        }
        return executeMessage;
    }

    private static Thread startProcStatusMemorySampler(Process process, AtomicBoolean sampling,
            AtomicLong maxMemoryKb) {
        File procStatusFile = getProcStatusFileIfPresent(process);
        if (procStatusFile == null) {
            return null;
        }
        Long pid = getPid(process);
        Thread t = new Thread(() -> {
            while (sampling.get()) {
                updateMaxMemoryKb(procStatusFile, maxMemoryKb);
                try {
                    Thread.sleep(5);
                } catch (InterruptedException e) {
                    break;
                }
            }
        });
        t.setDaemon(true);
        t.setName("proc-mem-sampler-" + pid);
        t.start();
        return t;
    }

    private static File getProcStatusFileIfPresent(Process process) {
        Long pid = getPid(process);
        if (pid == null || pid <= 0) {
            return null;
        }
        File f = new File("/proc/" + pid + "/status");
        if (!f.exists()) {
            return null;
        }
        return f;
    }

    private static Long getPid(Process process) {
        try {
            Field pidField = process.getClass().getDeclaredField("pid");
            pidField.setAccessible(true);
            Object value = pidField.get(process);
            if (value instanceof Integer) {
                return ((Integer) value).longValue();
            }
            if (value instanceof Long) {
                return (Long) value;
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    private static Long readProcStatusMemoryKb(File procStatusFile) {
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(new FileInputStream(procStatusFile), StandardCharsets.UTF_8))) {
            String line;
            Long rssKb = null;
            Long hwmKb = null;
            while ((line = br.readLine()) != null) {
                Matcher hwmMatcher = PROC_STATUS_VM_HWM_PATTERN.matcher(line);
                if (hwmMatcher.matches()) {
                    hwmKb = Long.parseLong(hwmMatcher.group(1));
                }
                Matcher rssMatcher = PROC_STATUS_VM_RSS_PATTERN.matcher(line);
                if (rssMatcher.matches()) {
                    rssKb = Long.parseLong(rssMatcher.group(1));
                }
                if (hwmKb != null && rssKb != null) {
                    break;
                }
            }
            if (hwmKb != null) {
                return hwmKb;
            }
            return rssKb;
        } catch (Exception e) {
            return null;
        }
    }

    private static void updateMaxMemoryKb(File procStatusFile, AtomicLong maxMemoryKb) {
        Long memKb = readProcStatusMemoryKb(procStatusFile);
        if (memKb != null && memKb > 0) {
            maxMemoryKb.getAndUpdate(prev -> Math.max(prev, memKb));
        }
    }
}
