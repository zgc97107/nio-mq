package org.zgc.nio.protocol;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

@AllArgsConstructor
@Data
public class NetworkReceive {
    private final int processor;
    private final String source;
    private final ByteBuffer buffer;

    public static NetworkReceive response(NetworkReceive request){
        String response = "received successfully";
        byte[] responseBytes = response.getBytes(StandardCharsets.UTF_8);
        ByteBuffer buffer = ByteBuffer.allocate(responseBytes.length + 4);
        buffer.putInt(responseBytes.length);
        buffer.put(responseBytes);
        buffer.rewind();
        return new NetworkReceive(request.processor, request.source, buffer);
    }
}
