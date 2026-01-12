package github.ttt.litesql.backend.dm.page;

import github.ttt.litesql.backend.dm.pageCache.PageCache;
import github.ttt.litesql.backend.utils.RandomUtils;

import java.util.Arrays;

/**
 * 特殊管理第一页（校验逻辑）
 * 每次数据库启动时，会生成一串随机字节，存储在 100 ~ 107 字节
 * 在正常数据库关闭时，会将这串字节拷贝到第一页的 108 ~ 115 字节
 * 数据库每次启动时，都会检查第一页的两处字节是否相同，用来判断上次是否正常关闭，是否需要进行数据的恢复流程
 */
public class PageOne {
    private static final int OF_VC = 100;
    private static final int LEN_VC = 8;

    /**
     * 初始化页面数据
     * 创建一个完整的页面大小的原始字节数组，并设置数据库开启时的验证字节
     * 返回值：初始化的原始数组
     * @return
     */
    public static byte[] InitRaw() {
        byte[] raw = new byte[PageCache.PAGE_SIZE];
        // 写入随机校验码
        setVcOpen(raw);
        return raw;
    }

    // 设置 "ValidCheck" 为打开状态
    public static void setVcOpen(Page pg){
        pg.setDirty(true);
        setVcOpen(pg.getData());
    }

    private static void setVcOpen(byte[] raw) {
        // 随机生成 8 字节数据，并拷贝到第一页的 100 ~ 107 字节
        System.arraycopy(RandomUtils.randomBytes(LEN_VC), 0, raw, OF_VC, LEN_VC);
    }

    public static void setVcClose(Page pg){
        pg.setDirty(true);
        setVcClose(pg.getData());
    }

    private static void setVcClose(byte[] raw) {
        System.arraycopy(raw, 0, raw, OF_VC + LEN_VC, LEN_VC);
    }

    public static boolean checkVc(Page pg) {
        return checkVc(pg.getData());
    }

    private static boolean checkVc(byte[] raw) {
        return Arrays.equals(Arrays.copyOfRange(raw, OF_VC, OF_VC + LEN_VC), Arrays.copyOfRange(raw, OF_VC + LEN_VC, OF_VC + 2 * LEN_VC));
    }


}
