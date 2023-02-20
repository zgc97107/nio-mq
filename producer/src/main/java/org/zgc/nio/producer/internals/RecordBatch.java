package org.zgc.nio.producer.internals;

import lombok.Data;
import lombok.extern.java.Log;
import org.zgc.nio.protocol.Record;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

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

    private void write(String message) {
        byte[] bytes = message.getBytes(StandardCharsets.UTF_8);
        int size = bytes.length;
        byteBuffer.putInt(size);
        byteBuffer.put(bytes);
        this.writeLimit = byteBuffer.position();
    }

    private boolean hasRoomFor(String message) {
        return writeLimit + message.getBytes(StandardCharsets.UTF_8).length > this.writeLimit;
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

    public boolean tryAppend(String message) {
        if (!writable){
            log.info("Appending record failed, batch is not writable");
            return false;
        }
        if (!this.hasRoomFor(message)) {
            log.info("Appending record failed, batch is not have enough room");
            return false;
        } else {
            this.write(message);
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
