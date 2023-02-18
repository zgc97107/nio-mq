package org.zgc.nio.producer.thread;

import lombok.Data;
import org.zgc.nio.parser.ResponseParser;
import org.zgc.nio.protocol.Record;
import org.zgc.nio.protocol.MethodInvokeResponse;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author lucheng
 * @since 2022/8/24
 */
@Data
public class Processor extends Thread {

    private Selector selector;
    private SocketChannel channel;
    private String host;
    private int port;
    boolean isStart = true;
    private SelectionKey key = null;
    private BlockingQueue<Record> waitingSendRequests = new LinkedBlockingQueue<>();
    private Map<Integer, MethodInvokeResponse> cachedResponse = new HashMap<>();
    private ReentrantLock lock = new ReentrantLock(true);
    private Condition condition = lock.newCondition();

    public Processor(String host, int port) {
        this.host = host;
        this.port = port;
        try {
            selector = Selector.open();
            channel = SocketChannel.open();
            channel.configureBlocking(false);
            channel.connect(new InetSocketAddress(host, port));
            channel.register(selector, SelectionKey.OP_CONNECT);
        } catch (Exception e) {
            System.out.println("processor create failed, exception: " + e);
        }
    }

    @Override
    public void run() {
        System.out.println("processor thread start successful: " + Thread.currentThread().getName());
        while (isStart) {
            int count = 0;
            try {
                count = selector.select(500);
            } catch (IOException e) {
                System.out.println("select failed, exception: " + e);
            }
            if (count <= 0)
                continue;
            Set<SelectionKey> keys = selector.selectedKeys();
            Iterator<SelectionKey> iterator = keys.iterator();
            while (iterator.hasNext()) {
                key = iterator.next();
                iterator.remove();
                if (key.isConnectable()) {
                    finishConnection(key);
                } else if (key.isWritable()) {
                    sendRequest(key);
                } else if (key.isReadable()) {
                    receiveResponse(key);
                }
            }
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
            System.out.println("connect server successful: " + channel.getRemoteAddress());
        } catch (InterruptedException | IOException e) {
            System.out.println("connect failed, exception" + e);
        }
    }

    private void sendRequest(SelectionKey key) {
        try {
            SocketChannel channel = (SocketChannel) key.channel();
            Record request;
            while ((request = this.waitingSendRequests.poll()) != null) {
                byte[] data = request.toByteArray();
                ByteBuffer buffer = ByteBuffer.allocate(data.length + 4);
                buffer.putInt(data.length);
                buffer.put(data);
                buffer.rewind();
                channel.write(buffer);
            }
            key.interestOps(SelectionKey.OP_READ);
        } catch (IOException e) {
            System.out.println("channel write failed, exception: " + e);
        }
    }

    private ResponseParser responseParser = null;

    public void receiveResponse(SelectionKey key) {
        try {
            SocketChannel channel = (SocketChannel) key.channel();
            if (this.responseParser == null) {
                this.responseParser = new ResponseParser();
            }
            MethodInvokeResponse response = responseParser.parse(channel);
            if (!responseParser.isFinish()) {
                return;
            }
            responseParser = null;
            cachedResponse.put(response.getRequestId(), response);
            lock.lock();
            try {
                condition.signalAll();
            } finally {
                lock.unlock();
            }
        } catch (IOException e) {
            System.out.println("channel read failed, exception: " + e);
        }
    }

    public void send(Record request) {
        boolean offer = waitingSendRequests.offer(request);
        if (!offer) {
            System.out.println("send request failed");
            return;
        }
        while (key == null) {
            try {
                TimeUnit.MILLISECONDS.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        System.out.println("poll request successful, waiting for response");
        key.interestOps(key.interestOps() | SelectionKey.OP_WRITE);

        while (!cachedResponse.containsKey(request.getRequestId())) {
            lock.lock();
            try {
                condition.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                lock.unlock();
            }
        }
        System.out.println("response info: " + cachedResponse.remove(request.getRequestId()));
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
