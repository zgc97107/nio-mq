package org.zgc.nio.server;

import lombok.extern.java.Log;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public class LogSegment {
    private long bytesSinceLastIndexEntry;

    private long indexIntervalBytes;

    private OffsetIndex index;

    private FileMessageSet log;

    public LogSegment(String filePath, long indexIntervalBytes) {
        this.indexIntervalBytes = indexIntervalBytes;
        this.bytesSinceLastIndexEntry = 0;
        this.index = new OffsetIndex(filePath + ".index", 0);
        this.log = new FileMessageSet(filePath + ".log");
    }

    public void append(Long offset, ByteBuffer message) {
        if (bytesSinceLastIndexEntry > indexIntervalBytes) {
            index.append(offset, (int) log.sizeInBytes());
            this.bytesSinceLastIndexEntry = 0;
        }
        log.append(message);
        this.bytesSinceLastIndexEntry += message.limit();
    }

}
