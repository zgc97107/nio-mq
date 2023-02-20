package org.zgc.nio.producer.internals;

import lombok.extern.java.Log;
import org.zgc.nio.producer.ProducerConfig;
import org.zgc.nio.protocol.Record;

import java.nio.ByteBuffer;
import java.util.Deque;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

@Log
public class RecordAccumulator {

    private final AtomicInteger appendsInProgress;
    private final AtomicInteger flushesInProgress;
    private final ConcurrentLinkedDeque<RecordBatch> batches;
    private final ConcurrentLinkedDeque<RecordBatch> incompletes;
    private final BufferPool bufferPool;

    public RecordAccumulator(BufferPool bufferPool) {
        this.batches = new ConcurrentLinkedDeque<>();
        this.incompletes = new ConcurrentLinkedDeque<>();
        this.appendsInProgress = new AtomicInteger(0);
        this.flushesInProgress = new AtomicInteger(0);
        this.bufferPool = bufferPool;
    }

    public boolean append(String message) throws InterruptedException, TimeoutException {
        appendsInProgress.incrementAndGet();
        try {
            synchronized (this.batches) {
                boolean result = tryAppend(message);
                if (result) {
                    return true;
                }
            }
            ByteBuffer buffer = bufferPool.allocate(ProducerConfig.RECORD_BATCH_BUFFER_SIZE, ProducerConfig.BUFFER_ALLOCATE_MAX_WAIT_TIME);
            synchronized (this.batches) {
                // 申请buffer期间，可能已有可用batch
                if (tryAppend(message)) {
                    bufferPool.deallocate(buffer);
                    return true;
                }
                RecordBatch batch = new RecordBatch(buffer);
                batches.addLast(batch);
                this.incompletes.add(batch);
                return batch.tryAppend(message);
            }
        } finally {
            appendsInProgress.decrementAndGet();
        }
    }

    public boolean tryAppend(String message) {
        RecordBatch last = batches.peekLast();
        if (last != null) {
            // 空间不足则创建新的batch，并关闭当前batch
            if (last.tryAppend(message)) {
                return true;
            } else {
                last.close();
            }
        }
        return false;
    }

    public synchronized RecordBatch ready() {
        long now = System.currentTimeMillis();
        RecordBatch recordBatch = batches.peekFirst();
        if (recordBatch == null || recordBatch.getRecords() <= 0) {
            return null;
        }
        if (now - recordBatch.getLastWriteTime() > ProducerConfig.RECORD_BATCH_SEND_MAX_WAIT_TIME) {
            return recordBatch;
        }
        if (recordBatch.freeSize() < ProducerConfig.RECORD_BATCH_SEND_FREE_SIZE) {
            return recordBatch;
        }
        return null;
    }

    public void deallocate(RecordBatch recordBatch){
        this.bufferPool.deallocate(recordBatch.getRecordBuffer());
    }
}
