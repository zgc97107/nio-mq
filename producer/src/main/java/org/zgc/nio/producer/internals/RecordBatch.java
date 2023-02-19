package org.zgc.nio.producer.internals;

import lombok.Data;
import lombok.extern.java.Log;
import org.zgc.nio.protocol.Record;

import java.nio.ByteBuffer;

@Log
public class RecordBatch {
    private final ByteBuffer byteBuffer;

    private int writeLimit;

    private final int initialCapacity;

    private boolean writable;

    private int records;

    private long lastWriteTime;

    public RecordBatch(ByteBuffer byteBuffer) {
        this.byteBuffer = byteBuffer;
        this.writeLimit = 0;
        this.initialCapacity = byteBuffer.capacity();
        this.writable = true;
        this.records = 0;
        this.lastWriteTime = System.currentTimeMillis();
    }

    private void write(Record record) {
        int size = record.getSerializedSize();
        byteBuffer.putInt(size);
        byteBuffer.put(record.toByteArray());
        this.writeLimit = byteBuffer.position();
    }

    private boolean hasRoomFor(Record record) {
        return writeLimit + record.getSerializedSize() > this.writeLimit;
    }

    public void close() {
        if (writable) {
            byteBuffer.flip();
            writable = false;
            log.info("Closing record batch");
        }
    }

    public int freeSize() {
        return initialCapacity - writeLimit;
    }

    public boolean tryAppend(Record record) {
        if (!writable){
            log.info("Appending record failed, batch is not writable");
            return false;
        }
        if (!this.hasRoomFor(record)) {
            log.info("Appending record failed, batch is not have enough room");
            return false;
        } else {
            this.write(record);
            this.records++;
            this.lastWriteTime = System.currentTimeMillis();
            return true;
        }

    }

    public long getLastWriteTime() {
        return lastWriteTime;
    }

    public int getRecords() {
        return records;
    }

    public ByteBuffer getRecordBuffer() {
        return byteBuffer;
    }
}
