package github.ttt.litesql.backend.common;

import com.sun.security.auth.module.Krb5LoginModule;
import github.ttt.litesql.commen.Error;

import java.util.HashMap;
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
    }

    protected T get(long key) throws Exception {
        while (true){
            lock.lock();
            if(getting.containsKey(key)) {
                // 请求的资源正在被其他线程获取
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
                // 资源在缓存中，直接返回
                T obj = cache.get(key);
                references.put(key, references.get(key) + 1);
                lock.unlock();
                return obj;
            }

            // 尝试获取该资源
            if(maxResource > 0 && count == maxResource) {
                lock.unlock();
                throw Error.CacheFullException;
            }
            count++;
            getting.put(key, true);
            lock.unlock();
            break;
        }

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

        lock.lock();
        getting.remove(key);
        cache.put(key, obj);
        references.put(key, 1);
        lock.unlock();

        return obj;
    }

    protected abstract T getForCache(long key) throws Exception;

}
