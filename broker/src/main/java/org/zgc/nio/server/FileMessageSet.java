package org.zgc.nio.server;

import com.sun.scenario.effect.Offset;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.concurrent.atomic.AtomicInteger;

public class FileMessageSet {
    private FileChannel channel;
    private File file;
    private int size;

    public FileMessageSet(String filePath) {
        try {
            this.file = new File(filePath);
            boolean newlyCreated = !file.exists();
            if (newlyCreated) {
                file.createNewFile();
            }
            RandomAccessFile raf = new RandomAccessFile(file, "rw");
            channel = raf.getChannel();
            this.size = (int) raf.length();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void append(ByteBuffer message) {
        int written = 0;
        try {
            int size = message.limit();
            while (written < size) {
                written += channel.write(message);
            }
            message.reset();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private int messageSizeLength = 4;
    private int offsetLength = 8;
    private int logOverheadLength = messageSizeLength + offsetLength;

    /**
     * 找到目标
     * @param targetOffset
     * @param startingPosition
     * @return
     */
    public OffsetPosition searchFor(long targetOffset, int startingPosition) {
        ByteBuffer buffer = ByteBuffer.allocate(logOverheadLength);
        long size = sizeInBytes();
        int position = startingPosition;
        try {
            while (position + logOverheadLength < size) {
                buffer.rewind();
                channel.read(buffer, position);
                if (buffer.hasRemaining()) {
                    throw new IllegalStateException(String.format("Failed to read complete buffer for targetOffset %d startPosition %d in %s",
                            targetOffset, startingPosition, file.getAbsolutePath()));
                }
                buffer.rewind();
                long offset = buffer.getLong();
                if (offset >= targetOffset) {
                    return new OffsetPosition(offset, position);
                }
                int messageSize = buffer.getInt();
                if (messageSize < Message.MIN_MESSAGE_OVER_HEAD) {
                    throw new IllegalStateException("Invalid message size: " + messageSize);
                }
                position += (logOverheadLength + messageSize);
            }
        }catch (Exception e){
            throw new RuntimeException("search for last offset failed",e);
        }
        return null;
    }

    public int sizeInBytes() {
        try {
            return (int) this.channel.position();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
