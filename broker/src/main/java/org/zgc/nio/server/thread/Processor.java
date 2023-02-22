package org.zgc.nio.server.thread;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.zgc.nio.protocol.NetworkReceive;
import org.zgc.nio.server.RequestChannel;
import org.zgc.nio.protocol.MethodInvokeResponse;
import org.zgc.nio.transport.NetworkChannel;

import java.io.IOException;
import java.nio.channels.*;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

/**
 * @author lucheng
 * @since 2022/8/24
 */
@Data
@Slf4j
public class Processor extends Thread {
    private int processorId;

    private Selector selector;

    private BlockingQueue<SocketChannel> newConnections;

    private Map<String, NetworkChannel> connections = new ConcurrentHashMap<>();


    private Map<String, BlockingQueue<MethodInvokeResponse>> waitingSendResponse;

    private RequestChannel requestChannel;

    public Processor(RequestChannel requestChannel, int processorId) throws IOException {
        this.selector = Selector.open();
        this.newConnections = new LinkedBlockingDeque<>();
        this.waitingSendResponse = new HashMap<>();
        Processor.processors.add(this);
        this.requestChannel = requestChannel;
        this.processorId = processorId;
    }

    private static int index = 0;
    private static List<Processor> processors = new ArrayList<>();

    public static void addChannel(SocketChannel channel) {
        // 轮询绑定
        int index = (Processor.index++) % processors.size();
        Processor processor = processors.get(index);
        processor.newConnections.offer(channel);
    }

    @Override
    public void run() {
        log.info("Processor thread started successful, processorId: " + processorId);
        while (true) {
            try {
                configureNewConnections();
                processNewResponses();
                poll();
            } catch (Exception e) {
                log.warn("processor execute error", e);
            }
        }
    }

    private void processNewResponses() {
        NetworkReceive response = requestChannel.receiveResponse(processorId);
        while (response != null) {
            try {
                // TODO response status
                sendResponse(response);
            } finally {
                response = requestChannel.receiveResponse(processorId);
            }
        }
    }

    private void sendResponse(NetworkReceive response) {
        String connectionId = response.getSource();
        NetworkChannel channel = connections.get(connectionId);
        channel.setSend(response.getBuffer());
    }


    private void poll() {
        try {
            int count = selector.select(500);
            if (count <= 0)
                return;
            Set<SelectionKey> keys = selector.selectedKeys();
            Iterator<SelectionKey> iterator = keys.iterator();
            while (iterator.hasNext()) {
                SelectionKey key = iterator.next();
                iterator.remove();
                SocketChannel channel = (SocketChannel) key.channel();
                String connectionId = connectionId(channel);
                // TODO 是否需要每次生成
                NetworkChannel networkChannel = connections.computeIfAbsent(connectionId, k -> new NetworkChannel(processorId, connectionId, key, channel));
                if (key.isReadable()) {
                    log.info("receive messages client: " + connectionId);
                    // 解决半包、沾包问题
                    NetworkReceive receive = networkChannel.read();
                    if (receive != null) {
                        requestChannel.sendRequest(receive);
                        return;
                    }
                } else if (key.isWritable()) {
                    networkChannel.write();
                }
            }
        } catch (Throwable e) {
            log.error("selector exception: " + e);
        }
    }

    private void configureNewConnections() {
        while (!newConnections.isEmpty()) {
            SocketChannel channel = newConnections.poll();
            try {
                String connectionId = connectionId(channel);
                channel.configureBlocking(false);
                SelectionKey key = channel.register(selector, SelectionKey.OP_READ);
                log.info("new connection: " + connectionId);
                connections.put(connectionId, new NetworkChannel(processorId, connectionId, key, channel));
            } catch (IOException e) {
                throw new RuntimeException("configureNewConnections failed", e);
            }
        }
    }


    public String connectionId(SocketChannel channel) {
        String localHost = channel.socket().getLocalAddress().getHostAddress();
        int localPort = channel.socket().getLocalPort();
        String remoteHost = channel.socket().getInetAddress().getHostAddress();
        int remotePort = channel.socket().getPort();
        return localHost + ":" + localPort + "-" + remoteHost + ":" + remotePort;
    }
}
