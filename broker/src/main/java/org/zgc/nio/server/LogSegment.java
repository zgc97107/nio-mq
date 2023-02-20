package org.zgc.nio.server;

import lombok.extern.java.Log;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public class LogSegment {
    private long bytesSinceLastIndexEntry;
    private final long indexIntervalBytes;
    private final OffsetIndex index;
    private final FileMessageSet log;
    private final long baseOffset;
    private long lastOffset;

    public LogSegment(OffsetIndex index, FileMessageSet log, long baseOffset, long indexIntervalBytes) {
        this.log = log;
        this.index = index;
        this.baseOffset = baseOffset;
        this.lastOffset = baseOffset;
        this.indexIntervalBytes = indexIntervalBytes;
        this.bytesSinceLastIndexEntry = 0;
    }

    public void append(ByteBuffer message) {
        // 是否需写稀疏索引
        if (bytesSinceLastIndexEntry > indexIntervalBytes) {
            index.append(nextOffset(), log.sizeInBytes());
            this.bytesSinceLastIndexEntry = 0;
        }
        log.append(message);
        this.bytesSinceLastIndexEntry += message.limit();
    }

    public long nextOffset() {
        return ++this.lastOffset;
    }

    public OffsetPosition translateOffset(long offset, int startingFilePosition) {
        OffsetPosition mapping = index.lookup(offset);
        return log.searchFor(offset, Math.max(mapping.getPosition(), startingFilePosition));
    }
}
