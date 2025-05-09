package github.ttt.litesql.backend.common;

import github.ttt.litesql.backend.utils.Panic;
import github.ttt.litesql.commen.Error;
import org.junit.Test;

import java.security.SecureRandom;
import java.util.Random;
import java.util.concurrent.CountDownLatch;

public class CacheTest {

    static Random random = new SecureRandom();

    private CountDownLatch cdl;
    private MockCache cache;

    @Test
    public void testCache() {
        cache = new MockCache();
        cdl = new CountDownLatch(200);

        // 记录测试开始时间
        long startTime = System.currentTimeMillis();
        System.out.println("Starting the cache test with 200 threads...");

        for(int i = 0; i < 200; i ++) {
            Runnable r = this::work;
            new Thread(r).start();
        }
        try {
            cdl.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // 记录测试结束时间
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        // 打印测试完成时间
        System.out.println("Cache test completed.");
        System.out.println("Total time taken: " + duration + " milliseconds.");
    }

    private void work() {
        for(int i = 0; i < 1000; i++) {
            long uid = random.nextInt();
            long h = 0;
            try {
                h = cache.get(uid);
            } catch (Exception e) {
                if(e == Error.CacheFullException) continue;
                Panic.panic(e);
            }
            assert h == uid;
            cache.release(h);
        }
        cdl.countDown();
    }
}