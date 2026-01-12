package github.ttt.litesql.backend.dm.page;

import github.ttt.litesql.backend.dm.pageCache.PageCache;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 页面是存储在内存中的数据单元，其结构包括：
 * PageNumber：页面的页号，从 1 开始计数。
 * data：实际包含的字节数。
 * dirty：标志着页面是否是脏页面，在缓存驱逐时，脏页面需要被写回磁盘。
 * lock：用于页面的锁。
 * PageCache：保存了一个 PageCache 的引用，方便在拿到 Page 的引用时可以快速对页面的缓存进行释放操作。
 */
public class pageImpl implements Page{
    private int pageNumber;
    private byte[] data;
    private boolean dirty;
    private Lock lock;

    private PageCache pc;

    public pageImpl(int pageNumber, byte[] data, PageCache pc) {
        this.pageNumber = pageNumber;
        this.data = data;
        this.pc = pc;
        lock = new ReentrantLock();
    }

    @Override
    public void lock() {
        lock.lock();
    }

    @Override
    public void unlock() {
        lock.unlock();
    }

    @Override
    public void release() {
        pc.release(this);
    }

    @Override
    public void setDirty(boolean dirty) {
        this.dirty = dirty;
    }

    @Override
    public boolean isDirty() {
        return dirty;
    }

    @Override
    public int getPageNumber() {
        return pageNumber;
    }

    @Override
    public byte[] getData() {
        return data;
    }
}
