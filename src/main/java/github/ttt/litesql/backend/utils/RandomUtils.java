package github.ttt.litesql.backend.utils;

import java.security.SecureRandom;
import java.util.Random;

/**
 * 生成指定长度的随机字节序列
 */
public class RandomUtils {
    public static byte[] randomBytes(int length) {
        Random r = new SecureRandom();
        byte[] buf = new byte[length];
        r.nextBytes(buf);
        return buf;
    }
}
