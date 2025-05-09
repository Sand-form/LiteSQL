package github.ttt.litesql.backend.common;

import com.sun.security.auth.module.Krb5LoginModule;
import github.ttt.litesql.commen.Error;

import java.util.HashMap;
import java.util.Set;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * AbstractCache 实现了一个引用计数策略的缓存
 */
public abstract class AbstractCache<T> {
    // 实际缓存的数据
    private HashMap<Long, T> cache;
    // 元素的引用个数
    private HashMap<Long, Integer> references;
    // 正在获取某资源的线程
    private HashMap<Long, Boolean> getting;

    // 缓存的最大资源数
    private int maxResource;
    // 缓存中元素的个数
    private int count = 0;
    private Lock lock;
    private Condition resourseAvailable;

    /**
     * 初始化
     * @param maxResource
     */
    public AbstractCache(int maxResource) {
        cache = new HashMap<>();
        references = new HashMap<>();
        getting = new HashMap<>();
        this.maxResource = maxResource;
        lock = new ReentrantLock();
        resourseAvailable = lock.newCondition();
    }

    /**
     * 从缓存中获取资源
     */
    protected T get(long key) throws Exception {
        // 循环获取资源  todo：可以优化一下
        while (true){
            lock.lock();
            if(getting.containsKey(key)) {
                // 如果其他线程正在获取这个资源，那么当前线程将等待 1ms，然后继续循环
                lock.unlock();
                try {
                    Thread.sleep(1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    continue;
                }
                continue;
            }

            if (cache.containsKey(key)) {
                // 资源在缓存中，直接返回，并增加引用计数
                T obj = cache.get(key);
                references.put(key, references.get(key) + 1);
                lock.unlock();
                return obj;
            }

            // 如果资源不在缓存中，尝试获取资源，如果缓存已满，抛出异常
            if(maxResource > 0 && count == maxResource) {
                lock.unlock();
                throw Error.CacheFullException;
            }
            count++;
            getting.put(key, true);
            lock.unlock();
            break;
        }

        // 尝试获取资源
        T obj = null;
        try {
            obj = getForCache(key);
        } catch (Exception e) {
            lock.lock();
            count--;
            getting.remove(key);
            lock.unlock();
            throw e;
        }

        // 将获取到的资源添加到缓存中，并设置引用计数为 1
        lock.lock();
        getting.remove(key);
        cache.put(key, obj);
        references.put(key, 1);
        lock.unlock();

        return obj;
    }

    /**
     * 强行释放一个缓存
     * @param key
     */
    protected void release(long key) {
        lock.lock();
        try {
            int ref = references.get(key) - 1;
            if(ref == 0) {
                T obj = cache.get(key);
                releaseForCache(obj);
                references.remove(key);
                cache.remove(key);
                count --;
            } else {
                references.put(key, ref);
            }
        } finally {
            lock.unlock();
        }
    }

    protected void close() {
        lock.lock();
        try {
            Set<Long> keys = cache.keySet();
            for(long key : keys) {
                T obj = cache.get(key);
                releaseForCache(obj);
                references.remove(key);
                cache.remove(key);
            }
        } finally {
            lock.unlock();
        }
    }

    /**
     * 当资源不存在时缓存的获取行为
     * @param key
     * @return
     * @throws Exception
     */
    protected abstract T getForCache(long key) throws Exception;

    /**
     * 当资源驱逐时的写回行为
     * @param obj
     */
    protected abstract void releaseForCache(T obj);

}
