package org.zgc.nio.parser;

import com.google.protobuf.InvalidProtocolBufferException;
import org.zgc.nio.protocol.Record;

import java.io.IOException;
import java.nio.channels.SocketChannel;

public class RequestParser extends Parser<Record> {
    @Override
    public Record parse(SocketChannel channel) throws IOException {
        return doParse(channel, bytes -> {
            try {
                if (bytes != null) {
                    return Record.parseFrom(bytes);
                }
            } catch (InvalidProtocolBufferException e) {
                e.printStackTrace();
            }
            return null;
        });
    }
}
