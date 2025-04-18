package github.ttt.litesql.backend.utils;

/**
 * 提供了一个用于处理异常并终止程序的工具方法
 * 通常用于处理严重异常（无法打开文件、数据库连接失败等），打印堆栈信息并终止程序
 */
public class Panic {
    public static void panic(Exception err) {
        err.printStackTrace();
        System.exit(1);
    }
}
