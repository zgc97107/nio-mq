package org.zgc.nio.server;

import lombok.extern.slf4j.Slf4j;

import java.nio.ByteBuffer;

@Slf4j
public class LogSegment {
    private long bytesSinceLastIndexEntry;
    private final long indexIntervalBytes;
    private final OffsetIndex indexFile;
    private final FileMessageSet logFile;
    private final long baseOffset;
    private long lastOffset;

    public LogSegment(OffsetIndex indexFile, FileMessageSet log, long baseOffset, long indexIntervalBytes) {
        this.logFile = log;
        this.indexFile = indexFile;
        this.baseOffset = baseOffset;
        this.lastOffset = baseOffset;
        this.indexIntervalBytes = indexIntervalBytes;
        this.bytesSinceLastIndexEntry = 0;
    }

    public void append(ByteBuffer message) {
        // 是否需写稀疏索引
        long offset = nextOffset();
        logFile.append(message);
        this.bytesSinceLastIndexEntry += message.limit();
        log.info("Append message, position: " + logFile.sizeInBytes()+", offset: " + offset +", sinceLastIndexEntry: " + bytesSinceLastIndexEntry);

        if (bytesSinceLastIndexEntry > indexIntervalBytes) {
            log.info("Write offset index, position: " + logFile.sizeInBytes());
            indexFile.append(offset, logFile.sizeInBytes());
            this.bytesSinceLastIndexEntry = 0;
        }
    }

    public long nextOffset() {
        return ++this.lastOffset;
    }

    public OffsetPosition translateOffset(long offset, int startingFilePosition) {
        OffsetPosition mapping = indexFile.lookup(offset);
        return logFile.searchFor(offset, Math.max(mapping.getPosition(), startingFilePosition));
    }
}
