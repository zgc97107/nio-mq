package org.zgc.nio.server.thread;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.zgc.nio.server.Service;
import org.zgc.nio.parser.RequestParser;
import org.zgc.nio.protocol.MethodInvokeRequest;
import org.zgc.nio.protocol.MethodInvokeResponse;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * @author lucheng
 * @since 2022/8/24
 */
@Data
@Slf4j
public class Processor extends Thread {

    private Selector selector;

    private BlockingQueue<SocketChannel> waitingBindChannel;

    private Map<String, BlockingQueue<MethodInvokeResponse>> waitingSendResponse;

    private Map<String, RequestParser> unFinishParsers;

    public Processor() throws IOException {
        selector = Selector.open();
        waitingBindChannel = new LinkedBlockingDeque<>();
        unFinishParsers = new HashMap<>();
        waitingSendResponse = new HashMap<>();
        Processor.processors.add(this);
    }

    private static int index = 0;
    private static List<Processor> processors = new ArrayList<>();

    public static void addChannel(SocketChannel channel) {
        // 轮询绑定
        int index = (Processor.index++) % processors.size();
        Processor processor = processors.get(index);
        processor.waitingBindChannel.offer(channel);
    }

    @Override
    public void run() {
        log.info("processor thread start successful");
        while (true) {
            bindingChannel();
            handle();
        }
    }

    public void handle() {
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
                String client = channel.getRemoteAddress().toString();
                if (key.isReadable()) {
                    log.info("receive request client: " + client);
                    // 解决半包、沾包问题
                    RequestParser parser = unFinishParsers.containsKey(client) ? unFinishParsers.remove(client) :
                            new RequestParser();
                    MethodInvokeRequest request = parser.parse(channel);
                    if (!parser.isFinish()) {
                        unFinishParsers.put(client, parser);
                        return;
                    }
                    log.info("parsed request : " + request);
                    if (request != null) {
                        // TODO 异步执行
                        MethodInvokeResponse response = Service.handlerRequest(request);
                        BlockingQueue<MethodInvokeResponse> waitingSendResponses = waitingSendResponse
                                .computeIfAbsent(client, k -> new LinkedBlockingDeque<>());
                        waitingSendResponses.offer(response);
                        // 添加对write事件的关注
                        key.interestOps(key.interestOps() | SelectionKey.OP_WRITE);
                    }
                } else if (key.isWritable()) {
                    BlockingQueue<MethodInvokeResponse> waitingSendResponses = waitingSendResponse
                            .computeIfAbsent(client, k -> new LinkedBlockingDeque<>());
                    MethodInvokeResponse response;
                    while ((response = waitingSendResponses.poll()) != null) {
                        byte[] data = response.toByteArray();
                        ByteBuffer buffer = ByteBuffer.allocate(data.length + 4);
                        buffer.putInt(data.length);
                        buffer.put(data);
                        buffer.rewind();
                        channel.write(buffer);
                        log.info("send response: " + response);
                    }
                    // 移除write事件的关注
                    key.interestOps(key.interestOps() & ~SelectionKey.OP_WRITE);
                }
            }
        } catch (Throwable e) {
            log.error("selector exception: " + e);
        }
    }

    private void bindingChannel() {
        SocketChannel channel = null;
        while ((channel = waitingBindChannel.poll()) != null) {
            try {
                channel.register(selector, SelectionKey.OP_READ);
            } catch (ClosedChannelException e) {
                e.printStackTrace();
            }
        }
    }
}
