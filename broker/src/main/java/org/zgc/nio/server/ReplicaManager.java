package org.zgc.nio.server;

import lombok.extern.java.Log;
import lombok.extern.slf4j.Slf4j;
import org.zgc.nio.protocol.Record;

import java.io.File;
import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
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
