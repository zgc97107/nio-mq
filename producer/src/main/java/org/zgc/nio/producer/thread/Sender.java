package org.zgc.nio.producer.thread;

import lombok.Data;
import org.zgc.nio.parser.ResponseParser;
import org.zgc.nio.producer.internals.RecordAccumulator;
import org.zgc.nio.producer.internals.RecordBatch;
import org.zgc.nio.protocol.Record;
import org.zgc.nio.protocol.MethodInvokeResponse;
import org.zgc.nio.reader.ChannelReader;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author lucheng
 * @since 2022/8/24
 */
@Data
public class Sender extends Thread {

    private Selector selector;
    private SocketChannel channel;
    private String host;
    private int port;
    boolean isStart = true;
    private SelectionKey key = null;
    private RecordBatch send = null;
    private String receive = null;
    private Map<Integer, MethodInvokeResponse> cachedResponse = new HashMap<>();
    private RecordAccumulator recordAccumulator;
    private ReentrantLock lock = new ReentrantLock(true);
    private Condition condition = lock.newCondition();

    public Sender(String host, int port, RecordAccumulator recordAccumulator) {
        this.recordAccumulator = recordAccumulator;
        this.host = host;
        this.port = port;
        try {
            selector = Selector.open();
            channel = SocketChannel.open();
            channel.configureBlocking(false);
            channel.connect(new InetSocketAddress(host, port));
            this.key = channel.register(selector, SelectionKey.OP_CONNECT);
        } catch (Exception e) {
            System.out.println("Sender create failed, exception: " + e);
        }
    }

    @Override
    public void run() {
        System.out.println("Sender start successful: " + Thread.currentThread().getName());
        while (isStart) {
            processReadyBatch();
            poll();
            processNewResponse();
        }
    }

    private void poll() {
        int count = 0;
        try {
            count = selector.select(500);
        } catch (IOException e) {
            System.out.println("select failed, exception: " + e);
        }
        if (count <= 0) {
            return;
        }
        Set<SelectionKey> keys = selector.selectedKeys();
        Iterator<SelectionKey> iterator = keys.iterator();
        while (iterator.hasNext()) {
            key = iterator.next();
            iterator.remove();
            try {
                if (key.isConnectable()) {
                    finishConnection(key);
                } else if (key.isWritable()) {
                    send(key);
                } else if (key.isReadable()) {
                    receive(key);
                }
            } catch (Exception e) {
                System.out.println("send failed, exception: " + e);
            }
        }
    }

    private void processReadyBatch() {
        if (send != null) {
            return;
        }
        RecordBatch readyBatch = recordAccumulator.ready();
        if (readyBatch == null) {
            return;
        }
        key.interestOps(key.interestOps() | SelectionKey.OP_WRITE);
        this.send = readyBatch;
    }

    private void processNewResponse() {
        if (this.receive != null) {
            System.out.println(this.receive);
            receive = null;
            this.key.interestOps(key.interestOps() & ~SelectionKey.OP_READ);
        }
    }

    private void finishConnection(SelectionKey key) {
        try {
            SocketChannel channel = (SocketChannel) key.channel();
            if (channel.isConnectionPending()) {
                while (!channel.finishConnect()) {
                    TimeUnit.MILLISECONDS.sleep(100);
                }
            }
            key.interestOps(key.interestOps() & ~SelectionKey.OP_READ);
            System.out.println("connect server successful: " + channel.getRemoteAddress());
        } catch (InterruptedException | IOException e) {
            System.out.println("connect failed, exception" + e);
        }
    }

    private void send(SelectionKey key) throws IOException {
        SocketChannel channel = (SocketChannel) key.channel();
        if (send != null) {
            ByteBuffer recordBuffer = send.getRecordBuffer();
            recordBuffer.rewind();
            channel.write(recordBuffer);
            recordAccumulator.deallocate(send);
        }
        send = null;
        key.interestOps(key.interestOps() & ~SelectionKey.OP_WRITE);
    }

    private void receive(SelectionKey key) throws IOException {
        if (receive == null) {
            SocketChannel channel = (SocketChannel) key.channel();
            ByteBuffer receiveBuffer = new ChannelReader(channel).read();
            receive = new String(receiveBuffer.array(), StandardCharsets.UTF_8);
            receiveBuffer.rewind();
        }
        key.interestOps(key.interestOps() & ~SelectionKey.OP_READ);
    }

    public void exit() {
        this.isStart = false;
        if (channel != null) {
            try {
                channel.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (key != null) {
            key.cancel();
        }
    }
}
