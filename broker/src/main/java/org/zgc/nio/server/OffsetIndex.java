package org.zgc.nio.server;

import lombok.AllArgsConstructor;
import lombok.extern.java.Log;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Log
public class OffsetIndex {
    private File file;
    private MappedByteBuffer mmap;
    private long baseOffset;
    private long lastOffset;
    private int entries;
    private int maxEntries;

    public OffsetIndex(String filePath, int baseOffset) {
        try {
            this.file = new File(filePath);
            boolean newlyCreated = !file.exists();
            if (newlyCreated) {
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
                log.info("Appending to index, offset: " + offset + ", position: " + position + ", lastOffset: " + lastOffset+", entries: " + entries);
            }
        }
    }

    public boolean isFull(){
        return this.entries >= maxEntries;
    }

    public void flush(){
        synchronized (this) {
            mmap.force();
        }
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

    @AllArgsConstructor
    static class OffsetPosition {
        long offset;
        int position;
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
