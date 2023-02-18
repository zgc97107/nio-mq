package org.zgc.nio.server;

import lombok.extern.slf4j.Slf4j;
import org.zgc.nio.server.thread.Acceptor;
import org.zgc.nio.server.thread.Processor;

import java.io.IOException;

/**
 * @author lucheng
 * @since 2022/8/24
 */
@Slf4j
public class Bootstrap {

    public static void main(String[] args) throws IOException {
        int port = 8848;
        int processorCount = 4;
        // 启动acceptor线程，用于监听channel建立请求
        new Acceptor(port).start();
        // 启动worker线程，用于处理请求、发送响应
        for (int i = 0; i < processorCount; i++) {
            new Processor().start();
        }
    }
}
