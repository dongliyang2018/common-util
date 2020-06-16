package com.dong.util;

/**
 * 字符串帮助类
 * @version 1.0 2020/6/16
 * @author dongliyang
 */
public abstract class StringUtil {

    public static boolean isNullEmpty(String val) {
        return val == null || val.trim().equals("");
    }

    public static boolean isNotNullEmpty(String val) {
        return !isNullEmpty(val);
    }
}
