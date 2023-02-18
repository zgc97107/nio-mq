package org.zgc.nio.parser;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.function.Function;

@Slf4j
@Data
public abstract class Parser<T> {

    private ByteBuffer lengthBuffer = null;
    private ByteBuffer dataBuffer = null;
    private boolean isFinish = false;

    public abstract T parse(SocketChannel channel) throws IOException;

    protected T doParse(SocketChannel channel, Function<ByteBuffer, T> function) {
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
        isFinish = true;
        // TODO 优化 异步执行
        return function.apply(dataBuffer);
    }
}
