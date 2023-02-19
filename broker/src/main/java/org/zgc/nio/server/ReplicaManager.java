package org.zgc.nio.server;

import lombok.extern.java.Log;
import org.zgc.nio.protocol.Record;

import java.io.File;
import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicInteger;

@Log
public class ReplicaManager {
    private LogSegment logSegment;

    public ReplicaManager(LogSegment logSegment) {
        this.logSegment = logSegment;
    }

    public void appendMessages(ByteBuffer buffer) {
        // TODO group by topic partition
        this.logSegment.append(buffer);
    }
}
