package com.dong;

import com.dong.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.concurrent.*;

/**
 * 执行命令
 * @version 1.0 2020/6/16
 * @author dongliyang
 */
public abstract class CommandExecutor {

    private static final Logger logger = LoggerFactory.getLogger(CommandExecutor.class);

    private static final ExecutorService processPool = Executors.newCachedThreadPool();

    /**
     * 执行命令，返回执行结果
     * @param command 命令
     * @param timeout 超时时间，毫秒值。如果小于等于0，则一直等待
     * @param charsetName 获取执行命令后的输出时使用的字符编码
     * @return
     */
    public static ExecuteResult executeCommand(String command, long timeout,String charsetName) {
        logger.debug("invoke executeCommand,command:{},timeout:{},charsetName:{}",command,timeout,charsetName);
        if (StringUtil.isNullEmpty(command)) {
            throw new IllegalArgumentException("command cannot be null or empty");
        }
        if (StringUtil.isNullEmpty(charsetName)) {
            throw new IllegalArgumentException("charsetName cannot be null or empty");
        }
        Process process = null;
        ExecuteResult executeResult = null;
        try {
            process = Runtime.getRuntime().exec(command);
            executeResult = doExec(process, command, timeout,charsetName);
        } catch (Throwable e) {
            logger.error("an exception has appeared when exec command:{},timeout:{},charsetName:{},exception message:{} ", command,
                    timeout,charsetName,e.getMessage());
            executeResult = new ExecuteResult(ExecuteResult.ExitCode.FAIL, null);
        }
        logger.debug("executeResult:{}",executeResult);
        return executeResult;
    }

    /**
     * 执行命令，返回执行结果
     * @param commandArray 命令数组
     * @param timeout 超时时间，毫秒值。如果小于等于0，则一直等待
     * @param charsetName 获取执行命令后的输出时使用的字符编码
     * @return
     */
    public static ExecuteResult executeCommand(String[] commandArray,long timeout,String charsetName) {
        logger.debug("invoke executeCommand,command:{},timeout:{},charsetName:{}",Arrays.toString(commandArray),timeout,charsetName);
        if (commandArray == null || commandArray.length == 0) {
            throw new IllegalArgumentException("commandArray cannot be null or empty");
        }
        if (StringUtil.isNullEmpty(charsetName)) {
            throw new IllegalArgumentException("charsetName cannot be null or empty");
        }
        Process process = null;
        ExecuteResult executeResult = null;
        try {
            process = Runtime.getRuntime().exec(commandArray);
            executeResult = doExec(process, Arrays.toString(commandArray), timeout,charsetName);
        } catch (IOException e) {
            logger.error("an exception has appeared when exec command:{},timeout:{},charsetName:{},exception message:{} ",
                    Arrays.toString(commandArray), timeout,charsetName,e.getMessage());
            executeResult = new ExecuteResult(ExecuteResult.ExitCode.FAIL, null);
        }
        logger.debug("executeResult:{}",executeResult);
        return executeResult;
    }

    private static ExecuteResult doExec(final Process process, final String command,final long timeout,String charsetName) {
        InputStream inputStream = null;
        InputStream errorStream = null;
        StreamReceiver outputStreamReceiver = null;
        StreamReceiver errorStreamReceiver = null;
        Future<Integer> executeFuture = null;

        try {
            process.getOutputStream().close();

            final String OUTPUT_STREAM_NAME = "OUTPUT";
            inputStream = process.getInputStream();
            outputStreamReceiver = new StreamReceiver(inputStream, OUTPUT_STREAM_NAME,charsetName);
            outputStreamReceiver.start();

            final String ERROR_STREAM_NAME = "ERROR";
            errorStream = process.getErrorStream();
            errorStreamReceiver = new StreamReceiver(errorStream,ERROR_STREAM_NAME,charsetName);
            errorStreamReceiver.start();

            Callable<Integer> call = new Callable<Integer>() {
                @Override
                public Integer call() throws Exception {
                    process.waitFor();
                    return process.exitValue();
                }
            };

            executeFuture = processPool.submit(call);
            int exitCode = 0;
            if (timeout <= 0) {
                exitCode = executeFuture.get();
            } else {
                exitCode = executeFuture.get(timeout, TimeUnit.MILLISECONDS);
            }
            return new ExecuteResult(exitCode, outputStreamReceiver.getContent() + errorStreamReceiver.getContent());

        } catch (IOException e) {
            logger.error("The command {" + command + "} execute failed,exception message:" + e.getMessage(),e);
            return new ExecuteResult(ExecuteResult.ExitCode.FAIL, null);
        } catch (TimeoutException  e) {
            logger.error("The command {"  + command + "} timeout. " + e.getMessage(),e);
            return new ExecuteResult(ExecuteResult.ExitCode.TIMEOUT, null);
        } catch (ExecutionException e) {
            logger.error( "The command {" + command + "} did not complete due to an execution error.", e);
            return new ExecuteResult(ExecuteResult.ExitCode.FAIL, null);
        } catch (InterruptedException e) {
            logger.error( "The command {" + command + "} did not complete due to an interrupted error.", e);
            return new ExecuteResult(ExecuteResult.ExitCode.FAIL, null);
        } catch (Exception e) {
            logger.error( "The command {" + command + "} did not complete due to an error.", e);
            return new ExecuteResult(ExecuteResult.ExitCode.FAIL, null);
        } finally {
            if(executeFuture != null) {
                try {
                    executeFuture.cancel(true);
                } catch (Exception e) {
                }
            }
            if(inputStream != null) {
                //关闭流
                closeQuietly(inputStream);
                if(outputStreamReceiver != null && !outputStreamReceiver.isInterrupted()) {
                    //中断线程
                    outputStreamReceiver.interrupt();
                }
            }

            if(errorStream != null) {
                closeQuietly(errorStream);
                if(errorStreamReceiver != null && !errorStreamReceiver.isInterrupted()) {
                    errorStreamReceiver.interrupt();
                }
            }

            if(process != null) {
                process.destroy();
            }
        }
    }

    private static void closeQuietly(Closeable closeable) {
        try {
            if (closeable != null) {
                closeable.close();
            }
        } catch (IOException e) {
            logger.error("an exception has appeared when invoke close method,exception message:{}", e.getMessage());
        }
    }
}
