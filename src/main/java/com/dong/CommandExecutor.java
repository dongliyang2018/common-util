package com.dong;

import com.dong.util.StringUtil;
import com.dong.util.SystemUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.*;

/**
 * 执行命令
 * @version 1.0 2020/6/16
 * @author dongliyang
 */
public abstract class CommandExecutor {

    private final static Logger logger = LoggerFactory.getLogger(CommandExecutor.class);

    /**
     * 执行命令
     * @param command 命令
     * @param timeout 超时时间，毫秒值。如果小于等于0，则一直等待
     * @return
     */
    public static ExecuteResult executeCommand(String command, long timeout) {
        logger.debug("invoke executeCommand,command:{},timeout:{}",command,timeout);
        if (StringUtil.isNullEmpty(command)) {
            throw new IllegalArgumentException("command cannot be null or empty");
        }

        return doExecute(command, timeout);
    }

    private static ExecuteResult doExecute(String command, long timeout) {
        Process process = null;
        String charsetName = null;

        try {
            if (SystemUtil.isRunOnWindows()) {
                /*
                特别注意: Runtime.getRuntime().exec() 有多个重载，注意分辨传字符串和字符串数组的区别。

                比如:
                Runtime.getRuntime().exec("ls");
                Runtime.getRuntime().exec("bash -c ls");
                Runtime.getRuntime().exec(new String[]{ "bash","-c","ls" });

                效果是一样的。

                但是主要的区别在于，字符串形式，不支持解析& | 特殊字符。

                比如
                Runtime.getRuntime().exec("echo hello && echo world");  //输出  hello && echo world。 跟在终端命令行执行的结果不一样

                使用字符串数组的形式，Runtime.getRuntime().exec(new String[]{ "bash","-c","echo hello && echo world" });
                输出:
                hello
                world

                字符串形式下Runtime.getRuntime().exec执行命令的时候无法解释&等特殊字符的本质是execvp特殊符号。
                而之所以数组情况能成是因为execvp调用了 /bin/bash ，/bin/bash 解释了 & , | 和execvp没关系。

                所以，为了更广泛的支持命令，这里使用数组的形式。
                 */
                process = Runtime.getRuntime().exec(new String[]{ "cmd.exe","/c",command });
                //Windows终端默认的字符编码是GBK，可以执行System.getenv()查看。
                charsetName = "GBK";
            } else {
                process = Runtime.getRuntime().exec(new String[]{ "bash", "-c", command });
                //Linux终端默认的字符编码是UTF-8，可以通过执行env命令查看。
                charsetName = "UTF-8";
            }

            ProgressStreamCallable streamGrabber = new ProgressStreamCallable(process, charsetName);
            ExecutorService executor = Executors.newSingleThreadExecutor();
            Future<ExecuteResult> processFuture = executor.submit(streamGrabber);
            executor.shutdown();

            ExecuteResult executeResult = null;
            if(timeout <= 0){
                executeResult = processFuture.get();
            } else {
                executeResult = processFuture.get(timeout, TimeUnit.MILLISECONDS);
            }
            logger.debug("exec finished. command result is:" + executeResult + ",it means " +
                    (executeResult.getExitCode() == ExecuteResult.ExitCode.SUCCESS ? "success" : "fail"));

            return executeResult;
        } catch (TimeoutException e) {
            logger.error("exec timeout. command:" + command + ",timeout:" + timeout + ",exception message:" + e.getMessage(),e);
            return new ExecuteResult(ExecuteResult.ExitCode.TIME_OUT, null);
        } catch (Throwable e) {
            logger.error("exec error. command:" + command + ",timeout:" + timeout + ",exception message:" + e.getMessage(),e);
            return new ExecuteResult(ExecuteResult.ExitCode.FAIL, "exception message:" + e.getMessage());
        } finally {
            if (process != null) {
                process.destroy();
            }
        }
    }
}
