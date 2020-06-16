package com.dong.util;

/**
 * 系统帮助类
 * @version 1.0 2020/6/16
 * @author dongliyang
 */
public abstract class SystemUtil {

    /**
     * 当前程序是否运行在Windows
     * @return
     */
    public static boolean isRunOnWindows() {
        return System.getProperty("os.name").toUpperCase().indexOf("WINDOWS") != -1;
    }

    /**
     * 当前程序是否运行在Linux
     * @return
     */
    public static boolean isRunOnLinux() {
        return !isRunOnWindows();
    }

    /**
     * 获取换行符
     * @return
     */
    public static String LS() {
        return System.getProperty("line.separator");
    }
}
