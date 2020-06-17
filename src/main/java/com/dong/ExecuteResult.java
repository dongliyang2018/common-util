package com.dong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.Arrays;

/**
 * 命令执行结果
 * @version 1.0 2020/6/16
 * @author dongliyang
 */
public class ExecuteResult implements Serializable {

    private static final long serialVersionUID = 3717271950532372193L;
    
    private final Logger logger = LoggerFactory.getLogger(ExecuteResult.class);

    /** 执行结果编码 */
    private int exitCode;
    /** 输出内容 */
    private String executeOutput;

    public ExecuteResult() { }

    public ExecuteResult(int exitCode, String executeOutput) {
        checkExitCode(exitCode);
        this.exitCode = exitCode;
        this.executeOutput = executeOutput;
    }

    public int getExitCode() {
        return exitCode;
    }

    public void setExitCode(int exitCode) {
        checkExitCode(exitCode);
        this.exitCode = exitCode;
    }

    public String getExecuteOutput() {
        return executeOutput;
    }

    public void setExecuteOutput(String executeOutput) {
        this.executeOutput = executeOutput;
    }


    @Override
    public String toString() {
        return "ExecuteResult{" +
                "exitCode=" + exitCode +
                ", executeOutput='" + executeOutput + '\'' +
                '}';
    }

    private void checkExitCode(int exitCode) throws IllegalArgumentException {
        if (!(ExitCode.SUCCESS == exitCode || ExitCode.FAIL == exitCode || ExitCode.TIMEOUT == exitCode)) {
            throw  new IllegalArgumentException("Incorrect value of exitCode,The allowed values are:" +
                    Arrays.asList(ExitCode.SUCCESS,ExitCode.FAIL,ExitCode.TIMEOUT) + ",but the current value is:" + exitCode);
        }
    }

    /**
     * 执行结果编码
     */
    interface ExitCode {
        /** 成功 */
        int SUCCESS = 0;
        /** 失败 */
        int FAIL = 1;
        /** 超时 */
        int TIMEOUT = -1;
    }
}
