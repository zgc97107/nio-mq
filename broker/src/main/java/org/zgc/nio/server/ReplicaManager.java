package org.zgc.nio.server;

import lombok.extern.java.Log;
import org.zgc.nio.protocol.Record;

import java.io.File;
import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicInteger;

@Log
public class ReplicaManager {
    private AtomicInteger offsetsIndex;
    private LogSegment logSegment;

    public ReplicaManager() {
        this.offsetsIndex = new AtomicInteger(0);
        String path = nextLogDir();
        File file = new File(path);
        if (!file.exists()) {
            file.mkdirs();
        }
        this.logSegment = new LogSegment(path, BrokerConfig.INDEX_INTERVAL_BYTES);
    }

    public void appendMessages(long offset, ByteBuffer buffer) {
        this.logSegment.append(offset, buffer);
    }

    public String nextLogDir() {
        return BrokerConfig.LOG_DIR + "_consumer_offsets-" + offsetsIndex.getAndIncrement();
    }
}
