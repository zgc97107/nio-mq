package org.zgc.nio.parser;

import com.google.protobuf.InvalidProtocolBufferException;
import org.zgc.nio.protocol.MethodInvokeResponse;

import java.io.IOException;
import java.nio.channels.SocketChannel;

public class ResponseParser extends Parser<MethodInvokeResponse> {
    @Override
    public MethodInvokeResponse parse(SocketChannel channel) throws IOException {
        return doParse(channel, bytes -> {
            try {
                if (bytes != null) {
                    return MethodInvokeResponse.parseFrom(bytes);
                }
            } catch (InvalidProtocolBufferException e) {
                e.printStackTrace();
            }
            return null;
        });
    }
}
