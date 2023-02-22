package org.zgc.nio.transport;

import com.sun.org.apache.bcel.internal.generic.Select;
import lombok.extern.slf4j.Slf4j;
import org.zgc.nio.protocol.NetworkReceive;

import java.io.EOFException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

@Slf4j
public class NetworkChannel {
    private int processor;
    private String connectionId;
    private SocketChannel channel;
    private SelectionKey key;
    private ByteBuffer receiveSize = null;
    private ByteBuffer receive = null;
    private ByteBuffer send = null;

    public NetworkChannel(int processor, String connectionId, SelectionKey key, SocketChannel channel) {
        this.processor = processor;
        this.connectionId = connectionId;
        this.channel = channel;
        this.key = key;
    }

    public synchronized void setSend(ByteBuffer send) {
        if (this.send != null) {
            throw new IllegalStateException("Attempt to begin a send operation with prior send operation still in progress.");
        }
        this.send = send;
        this.key.interestOps(key.interestOps() | SelectionKey.OP_WRITE);
    }

    public void mute() {
        key.interestOps(key.interestOps() & ~SelectionKey.OP_READ);
    }

    public void unmute() {
        key.interestOps(key.interestOps() | SelectionKey.OP_READ);
    }

    public synchronized NetworkReceive read() {
        try {
            // 是否有未读取完的buffer
            if (receiveSize == null) {
                receiveSize = ByteBuffer.allocate(4);
            }
            channel.read(receiveSize);
            if (receiveSize.position() == 0) {
                channel.close();
                receiveSize = null;
                throw new Exception("channel closed" + channel.getRemoteAddress());
            }
            // 出现半包情况，等待下次read事件将buffer写满
            if (receiveSize.hasRemaining()) {
                log.info("half package cached");
                return null;
            }
            receiveSize.rewind();
            int length = receiveSize.getInt();
            if (receive == null) {
                // 解决沾包问题
                // 只申请固定长度的ByteBuffer，避免读取到下个包的内容
                receive = ByteBuffer.allocate(length);
            }
            channel.read(receive);
            if (receive.hasRemaining()) {
                log.info("half package cached");
                return null;
            }
            receive.rewind();
        } catch (Exception e) {
            log.error("parse error exception: " + e);
        }
        NetworkReceive networkReceive = new NetworkReceive(processor, connectionId, receive);
        clear();
        mute();
        return networkReceive;
    }

    private synchronized void clear() {
        receiveSize = null;
        receive = null;
    }

    public void write() throws IOException {
        if (this.send == null) {
            return;
        }
        while (send.hasRemaining()){
            channel.write(send);
        }
        this.send = null;
        key.interestOps(key.interestOps() & ~SelectionKey.OP_WRITE);
        unmute();
    }
}
