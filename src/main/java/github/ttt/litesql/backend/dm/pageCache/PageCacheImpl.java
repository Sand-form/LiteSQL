package github.ttt.litesql.backend.dm.pageCache;

import github.ttt.litesql.backend.common.AbstractCache;
import github.ttt.litesql.backend.dm.page.Page;
import github.ttt.litesql.backend.dm.page.pageImpl;
import github.ttt.litesql.backend.utils.Panic;
import github.ttt.litesql.commen.Error;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class PageCacheImpl extends AbstractCache<Page> implements PageCache {

    private static final int MEN_MIN_LTM = 10;
    public static final String DB_SUFFIX = ".db";

    private RandomAccessFile file;
    private FileChannel fc;
    private Lock fileLock;

    private AtomicInteger pageNumbers;

    /**
     * 初始化
     *
     * @param maxResource
     */
    public PageCacheImpl(RandomAccessFile file, FileChannel fileChannel, int maxResource) {
        super(maxResource);
        if (maxResource < MEN_MIN_LTM) {
            Panic.panic(Error.MemTooSmallException);
        }
        long length = 0;
        try {
            length = file.length();
        } catch (IOException e) {
            Panic.panic(e);
        }
        this.file = file;
        this.fc = fileChannel;
        this.fileLock = new ReentrantLock();
        this.pageNumbers = new AtomicInteger((int) length / PAGE_SIZE);
    }

    /**
     * 根据 pageNumber 从数据库文件中读取页数据，并包裹成 Page
     * @param key
     * @return
     * @throws Exception
     */
    @Override
    protected Page getForCache(long key) throws Exception {
        int pgno = (int) key;
        long offset = PageCacheImpl.pageOffset(pgno);

        ByteBuffer buf = ByteBuffer.allocate(PAGE_SIZE);
        fileLock.lock();
        try {
            fc.position(offset);
            fc.read(buf);
        } catch (IOException e) {
            Panic.panic(e);
        }
        fileLock.unlock();
        return new pageImpl(pgno, buf.array(), this);
    }

    private static long pageOffset(int pgno) {
        return (pgno - 1) * PAGE_SIZE;
    }

    @Override
    protected void releaseForCache(Page pg) {
        if (pg.isDirty()) {
            flush(pg);
        }
    }

    private void flush(Page pg) {
        int pgno = pg.getPageNumber();
        long offset = pageOffset(pgno);

        fileLock.lock();
        try {
            ByteBuffer buf = ByteBuffer.wrap(pg.getData());
            fc.position(offset);
            fc.write(buf);
            fc.force(false);
        } catch (IOException e) {
            Panic.panic(e);
        } finally {
            fileLock.unlock();
        }
    }

    /**
     * 创建一个新的页面，并将初始数据写入该页面
     * 然后将该页面刷新到磁盘，并返回新页面的页号
     *
     * @param initData 用于初始化新页面的字节数组数据
     * @return 新创建页面的页号
     */
    @Override
    public int newPage(byte[] initData) {
        // 原子性的增加页号，并获取新的页号
        int pgno = pageNumbers.incrementAndGet();
        Page pg = new pageImpl(pgno, initData, null);
        flushPage(pg);
        return pgno;
    }

    @Override
    public Page getPage(int pgno) throws Exception {
        return get((long) pgno);
    }

    @Override
    public void close() {
        super.close();
        try {
            fc.close();
            file.close();
        } catch (IOException e) {
            Panic.panic(e);
        }
    }

    @Override
    public void release(Page page) {
        release((long) page.getPageNumber());
    }

    @Override
    public void truncateByBgno(int maxPgno) {
        long size = pageOffset(maxPgno + 1);
        try {
            file.setLength(size);
        } catch (IOException e) {
            Panic.panic(e);
        }
        pageNumbers.set(maxPgno);
    }

    @Override
    public int getPageNumber() {
        return pageNumbers.intValue();
    }

    @Override
    public void flushPage(Page pg) {
        flush(pg);
    }
}
