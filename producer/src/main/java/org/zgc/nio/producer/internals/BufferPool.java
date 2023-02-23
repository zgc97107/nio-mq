package org.zgc.nio.producer.internals;

import lombok.extern.java.Log;

import java.nio.ByteBuffer;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

@Log
public class BufferPool {
    private final long totalMemory;

    private final int poolSize;

    private final ReentrantLock lock;

    private final Deque<ByteBuffer> free;

    private final Deque<Condition> waiters;

    private long availableMemory;

    public BufferPool(long memory, int poolSize) {
        this.totalMemory = memory;
        this.poolSize = poolSize;
        this.lock = new ReentrantLock();
        this.availableMemory = memory;
        this.free = new ArrayDeque<>(poolSize);
        this.waiters = new ArrayDeque<>(poolSize);
        log.info("BufferPool initialized, poolSize=" + poolSize + ", availableMemory=" + availableMemory);
    }

    /**
     * 申请buffer
     *
     * @param size
     * @param maxWaitTime
     * @return
     * @throws InterruptedException
     * @throws TimeoutException
     */
    public ByteBuffer allocate(int size, long maxWaitTime) throws InterruptedException, TimeoutException {
        if (size > this.totalMemory) {
            throw new IllegalArgumentException("Allocated memory more than " + this.totalMemory);
        }
        this.lock.lock();
        try {
            // 申请大小与默认大小相同，并且有空闲buffer，直接返回
            if (size == poolSize && !this.free.isEmpty()) {
                log.info("Allocating successfully by frees, size: " + size);
                return this.free.pollFirst();
            }
            int freeListSize = this.free.size() * this.poolSize;
            // 需要申请新buffer，检查可用空间大小
            if (this.availableMemory + freeListSize >= this.totalMemory) {
                log.info("Allocating successfully by availableMemory, size: " + size);
                // 空间充足，直接申请
                freeUp(size);
                this.availableMemory -= size;
                lock.unlock();
                return ByteBuffer.allocateDirect(size);
            } else {
                // 空间不足，进入等待
                Condition moreAccumulated = this.lock.newCondition();
                this.waiters.addLast(moreAccumulated);
                int accumulated = 0;
                boolean waitingTimeElapsed;
                ByteBuffer byteBuffer = null;
                while (accumulated < size) {
                    long start = System.currentTimeMillis();
                    long time;
                    try {
                        waitingTimeElapsed = moreAccumulated.await(maxWaitTime, TimeUnit.MILLISECONDS);
                    } catch (InterruptedException e) {
                        this.waiters.remove(moreAccumulated);
                        throw e;
                    } finally {
                        time = System.currentTimeMillis() - start;
                        time = Math.max(0, time);
                    }

                    if (waitingTimeElapsed) {
                        this.waiters.remove(moreAccumulated);
                        throw new TimeoutException("Allocated memory failed waiting time more than " + waitingTimeElapsed);
                    }
                    // 重新构建等待时间
                    maxWaitTime -= time;
                    // buffer大小与默认大小相同，且等待期间释放内存为0、已有可用buffer
                    if (accumulated == 0 && size == this.poolSize && !this.free.isEmpty()) {
                        byteBuffer = this.free.pollLast();
                        accumulated = size;
                        log.info("Allocating successfully by waiting for free, size: " + size);
                    } else {
                        freeUp(size - accumulated);
                        int got = (int) Math.min(size - accumulated, this.availableMemory);
                        this.availableMemory -= got;
                        accumulated += got;
                    }
                }

                Condition removed = this.waiters.removeFirst();
                // 如果拿到可用空间的不是第一个进入等待的，说明有某种特殊力量
                if (removed != moreAccumulated) {
                    throw new IllegalStateException("Allocated memory warning, this shouldn't happen");
                }

                // 当前线程申请之后，仍有可用空间，并有其他线程在等待，则唤醒其他线程
                if (this.availableMemory > 0 || !this.free.isEmpty()) {
                    if (!this.waiters.isEmpty()) {
                        this.waiters.peekFirst().signal();
                    }
                }

                lock.unlock();
                if (byteBuffer == null) {
                    log.info("Allocating successfully by waiting for availableMemory");
                    return ByteBuffer.allocate(size);
                } else {
                    return byteBuffer;
                }
            }

        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    /**
     * 释放空闲buffer，增加可用空间
     *
     * @param size
     */
    private void freeUp(int size) {
        while (!this.free.isEmpty() && this.availableMemory < size) {
            this.availableMemory += this.free.pollLast().capacity();
        }
    }

    /**
     * 归还buffer
     *
     * @param byteBuffer
     */
    public void deallocate(ByteBuffer byteBuffer) {
        deallocate(byteBuffer, byteBuffer.capacity());
    }

    public void deallocate(ByteBuffer byteBuffer, int size) {
        lock.lock();
        try {
            if (size == this.poolSize && size == byteBuffer.capacity()) {
                byteBuffer.clear();
                this.free.add(byteBuffer);
                log.info("Deallocated default pool size: " + this.poolSize);
            } else {
                this.availableMemory += size;
                log.info("Deallocated customer pool size: " + this.poolSize + " available: " + availableMemory);
            }
            Condition moreAvailable = this.waiters.peekFirst();
            if (moreAvailable != null) {
                moreAvailable.signal();
                log.info("Signal accumulated memory thread");
            }
        } finally {
            lock.unlock();
        }
    }
}
