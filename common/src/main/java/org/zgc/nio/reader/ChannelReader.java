package org.zgc.nio.reader;

import lombok.extern.slf4j.Slf4j;

import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

@Slf4j
public class ChannelReader {
    private SocketChannel channel;
    private ByteBuffer lengthBuffer = null;
    private ByteBuffer dataBuffer = null;

    public ChannelReader(SocketChannel channel) {
        this.channel = channel;
    }

    public ByteBuffer read(){
        try {
            // 是否有未读取完的buffer
            if (lengthBuffer == null) {
                lengthBuffer = ByteBuffer.allocate(4);
            }
            channel.read(lengthBuffer);
            if (lengthBuffer.position() == 0) {
                channel.close();
                throw new Exception("channel closed" + channel.getRemoteAddress());
            }
            // 出现半包情况，等待下次read事件将buffer写满
            if (lengthBuffer.hasRemaining()) {
                log.info("half package cached");
                return null;
            }
            lengthBuffer.rewind();
            int length = lengthBuffer.getInt();
            if (dataBuffer == null) {
                // 解决沾包问题
                // 只申请固定长度的ByteBuffer，避免读取到下个包的内容
                dataBuffer = ByteBuffer.allocate(length);
            }
            channel.read(dataBuffer);
            if (dataBuffer.hasRemaining()) {
                log.info("half package cached");
                return null;
            }
            dataBuffer.rewind();
        } catch (Exception e) {
            log.error("parse error exception: " + e);
            dataBuffer = null;
        }
        return dataBuffer;
    }
}
