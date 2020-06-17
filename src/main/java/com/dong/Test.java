package com.dong;

/**
 * @version 1.0 2020/6/17
 * @author dongliyang
 */
public class Test {
    public static void main(String[] args) {
        String command = "dir";
        String[] commandArr = new String[]{ "dir" };
        long timeout = 5 * 60 * 1000;
        String charsetName = "GBK";
        CommandExecutor.executeCommand("dir", timeout, charsetName);
        CommandExecutor.executeCommand(commandArr, timeout, charsetName);
        commandArr = new String[]{ "cmd.exe","/c","dir" };
        CommandExecutor.executeCommand(commandArr, timeout, charsetName);
    }
}
