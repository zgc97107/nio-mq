package org.zgc.nio.server.thread;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

/**
 * @author lucheng
 * @since 2022/8/24
 */
@Slf4j
public class Acceptor extends Thread {

    private Selector selector;

    private ServerSocketChannel channel;

    public Acceptor(int port) throws IOException {
        channel = ServerSocketChannel.open();
        channel.configureBlocking(false);
        channel.socket().bind(new InetSocketAddress(port));
        selector = Selector.open();
        this.channel.register(selector, SelectionKey.OP_ACCEPT);
        log.info("server start successful, binding port:" + port);
    }

    @Override
    public void run() {
        log.info("acceptor thread start successful");
        try {
            while (true) {
                int count = selector.select(500);
                if (count <= 0)
                    continue;
                Set<SelectionKey> keys = selector.selectedKeys();
                Iterator<SelectionKey> iterator = keys.iterator();
                while (iterator.hasNext()) {
                    SelectionKey key = iterator.next();
                    iterator.remove();
                    if (key.isAcceptable()) {
                        ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();
                        SocketChannel channel = serverSocketChannel.accept();
                        channel.configureBlocking(false);
                        // 将链接事件绑定到某个Processor
                        Processor.addChannel(channel);
                        log.info("connection accepted: " + channel.getRemoteAddress());
                    }
                }
            }
        } catch (Exception e) {
            log.error("select exception: " + e);
        }
    }
}
