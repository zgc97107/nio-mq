package org.zgc.nio.server;

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

    public FileMessageSet(String filePath) {
        try {
            this.file = new File(filePath);
            boolean newlyCreated = !file.exists();
            if (newlyCreated) {
                file.createNewFile();
            }
            channel = new RandomAccessFile(file, "rw").getChannel();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void append(ByteBuffer message){
        int written = 0;
        try {
            int size = message.limit();
            while (written < size) {
                written += channel.write(message);
            }
            message.reset();
        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }

    public long sizeInBytes() {
        try {
            return this.channel.position();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
