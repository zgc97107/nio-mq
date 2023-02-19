package org.zgc.nio.protocol;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.nio.ByteBuffer;

@Data
@AllArgsConstructor
public class Message {
    private String connectionId;
    private ByteBuffer message;
}
