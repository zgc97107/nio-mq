package org.zgc.nio.parser;

import com.google.protobuf.InvalidProtocolBufferException;
import org.zgc.nio.protocol.MethodInvokeRequest;

import java.io.IOException;
import java.nio.channels.SocketChannel;

public class RequestParser extends Parser<MethodInvokeRequest> {
    @Override
    public MethodInvokeRequest parse(SocketChannel channel) throws IOException {
        return doParse(channel, bytes -> {
            try {
                if (bytes != null) {
                    return MethodInvokeRequest.parseFrom(bytes);
                }
            } catch (InvalidProtocolBufferException e) {
                e.printStackTrace();
            }
            return null;
        });
    }
}
