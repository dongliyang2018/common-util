package com.dong;

import com.dong.util.StringUtil;
import com.dong.util.SystemUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.concurrent.Callable;

/**
 * 从流中获取内容
 * @version 1.0 2020/6/16
 * @author dongliyang
 */
public class ProgressStreamCallable implements Callable<ExecuteResult> {
    
    private final Logger logger = LoggerFactory.getLogger(ProgressStreamCallable.class);

    private Process process;
    private String charsetName;

    public ProgressStreamCallable(Process process, String charsetName) {
        if (process == null) {
            throw new IllegalArgumentException("process cannot be null");
        }
        if (StringUtil.isNullEmpty(charsetName)) {
            throw new IllegalArgumentException("charsetName cannot be null or empty");
        }
        this.process = process;
        this.charsetName = charsetName;
    }

    @Override
    public ExecuteResult call() throws Exception {

        logger.trace("invoke call method,charsetName:{}",charsetName);
        BufferedReader standOutReader = new BufferedReader(new InputStreamReader(process.getInputStream(),charsetName));
        BufferedReader standErrorReader = new BufferedReader(new InputStreamReader(process.getErrorStream(),charsetName));

        String line = null;
        StringBuilder output = new StringBuilder();
        while ((line = standOutReader.readLine()) != null) {
            output.append(line).append(SystemUtil.LS());
        }

        StringBuilder error = new StringBuilder();
        while ((line = standErrorReader.readLine()) != null) {
            error.append(line).append(SystemUtil.LS());
        }

        standOutReader.close();
        standErrorReader.close();

        //waitFor，阻塞process进程，直到外部进程执行完，获取外部进程执行结果
        int originalExitCode = process.waitFor();
        String executeOutput = output.length() != 0 ? output.toString() : error.toString();
        logger.trace("originalExitCode:{},executeOutput:{}",originalExitCode,executeOutput);

        //0代表程序正常退出，非0代表程序执行错误。这里将其他的非0值(经过测试，执行失败exitCode是1，但是为了保险起见，还是做了这个判断)，转成1，代表执行失败。
        int exitCode = ExecuteResult.ExitCode.SUCCESS == originalExitCode ? ExecuteResult.ExitCode.SUCCESS : ExecuteResult.ExitCode.FAIL;
        ExecuteResult executeResult = new ExecuteResult(exitCode, executeOutput);

        logger.trace("executeResult:{}", executeResult);
        return executeResult;
    }
}
