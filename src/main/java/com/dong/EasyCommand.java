package com.dong;

import com.dong.util.SystemUtil;

/**
 *
 * @version 1.0 2020/6/17
 * @author dongliyang
 */
public abstract class EasyCommand {

    public static ExecuteResult executeCommand(String command, long timeout) {
        String[] cmdArray = null;
        String charsetName = null;
        if (SystemUtil.isRunOnWindows()) {
            //字符串数组的形式，第一个参数是要执行的命令，需要指定
            cmdArray = new String[]{ "cmd.exe","/c",command };
            charsetName = "GBK";
        } else {
            cmdArray = new String[]{ "bash","-c",command };
            charsetName = "UTF-8";
        }
        return CommandExecutor.executeCommand(cmdArray, timeout, charsetName);
    }


    /*
    特别注意: Runtime.getRuntime().exec() 有多个重载，注意分辨传字符串和字符串数组的区别。
    比如:
    Runtime.getRuntime().exec("ls");
    Runtime.getRuntime().exec("bash -c ls");
    Runtime.getRuntime().exec(new String[]{ "bash","-c","ls" });

    Runtime.getRuntime().exec("/app/test.sh");
    Runtime.getRuntime().exec("bash -c /app/test.sh");
    Runtime.getRuntime().exec(new String[]{ "bash","-c","/app/test.sh" });

    效果是一样的。

    但是主要的区别在于，字符串形式，不支持解析& | 特殊字符。 (也就说说，通常可以使用字符串形式的就可以了，除非执行的命令包含& |特殊字符，才需要使用字符串数组的形式)
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
}
