package org.zgc.nio.server;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.java.Log;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Slf4j
@Data
public class OffsetIndex {
    private File file;
    private MappedByteBuffer mmap;
    private long baseOffset;
    private long lastOffset;
    private int entries;
    private int maxEntries;

    public OffsetIndex(String filePath, long baseOffset) {
        try {
            this.file = new File(filePath);
            boolean newlyCreated = !file.exists();
            if (newlyCreated) {
                log.info("create new offset index file: " + filePath);
                file.createNewFile();
            }
            RandomAccessFile raf = new RandomAccessFile(file, "rw");
            raf.setLength(roundToExactMultiple(BrokerConfig.INDEX_FILE_SIZE, 8));
            if (newlyCreated) {
                raf.setLength(1024);
            }
            long len = raf.length();
            this.mmap = raf.getChannel().map(FileChannel.MapMode.READ_WRITE, 0, len);
            if (newlyCreated) {
                mmap.position(0);
            } else {
                mmap.position(roundToExactMultiple(mmap.limit(), 8));
            }
            this.entries = mmap.position() / 8;
            this.maxEntries = mmap.limit() / 8;
            this.baseOffset = baseOffset;
            this.lastOffset = baseOffset + entries;
        } catch (IOException e) {
            throw new RuntimeException("OffsetIndex file not found", e);
        }
    }

    public void append(long offset, int position) {
        synchronized (this) {
            if (entries == 0 || offset > lastOffset) {
                mmap.putInt((int) (offset - baseOffset));
                mmap.putInt(position);
                entries++;
                lastOffset = offset;
                log.info("Appending to index, offset: " + offset + ", position: " + position + ", lastOffset: " + lastOffset + ", entries: " + entries);
            }
        }
    }

    public OffsetPosition lookup(long targetOffset) {
        synchronized (this) {
            ByteBuffer idx = mmap.duplicate();
            int slot = indexSlotFor(idx, targetOffset);
            if (slot ==-1){
                return new OffsetPosition(baseOffset, 0);
            }else {
                return new OffsetPosition(baseOffset + relativeOffset(idx, slot), physical(idx, slot));
            }
        }

    }

    public boolean isFull() {
        return this.entries >= maxEntries;
    }

    public void flush() {
        synchronized (this) {
            mmap.force();
        }
    }

    /**
     * 二分查找
     *
     * @param idx
     * @param targetOffset
     * @return
     */
    private int indexSlotFor(ByteBuffer idx, long targetOffset) {
        long relOffset = targetOffset - baseOffset;
        if (entries == 0) {
            return -1;
        }

        if (relativeOffset(idx, 0) > relOffset) {
            return -1;
        }

        int lo = 0;
        int hi = entries - 1;
        while (lo < hi) {
            int mid = (hi + lo) / 2;
            int found = relativeOffset(idx, mid);
            if (found == relOffset) {
                return mid;
            }else if (found < relOffset) {
                lo = mid;
            }else {
                hi = mid - 1;
            }
        }
        return lo;
    }

    private OffsetPosition readLastEntry() {
        synchronized (this) {
            if (this.entries == 0) {
                return new OffsetPosition(baseOffset, 0);
            } else {
                return new OffsetPosition(baseOffset + relativeOffset(mmap, this.entries - 1), physical(mmap, this.entries - 1));
            }
        }
    }

    private int relativeOffset(ByteBuffer buffer, int n) {
        return buffer.getInt(n * 8);
    }

    private int physical(ByteBuffer buffer, int n) {
        return buffer.getInt(n * 8 + 4);
    }

    /**
     * 除去余数
     * roundToExactMultiple(67, 8) == 64
     *
     * @param number
     * @param factor
     * @return
     */
    private int roundToExactMultiple(int number, int factor) {
        return factor * (number / factor);
    }
}
