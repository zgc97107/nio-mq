package org.zgc.nio.parser;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.zgc.nio.reader.ChannelReader;

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
        ByteBuffer buffer = new ChannelReader(channel).read();
        if (buffer == null) {
            return null;
        }
        return function.apply(buffer);
    }
}
