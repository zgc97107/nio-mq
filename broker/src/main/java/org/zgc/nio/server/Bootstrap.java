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
        LogManager logManager = new LogManager();
        LogSegment logSegment = logManager.logSegment();
        ReplicaManager replicaManager = new ReplicaManager(logSegment);
        RequestChannel requestChannel = new RequestChannel(BrokerConfig.PROCESSOR_SIZE);
        RequestHandler requestHandler = new RequestHandler(requestChannel, replicaManager);
        new Thread(requestHandler).start();
        new Acceptor(BrokerConfig.PORT).start();
        for (int i = 0; i < BrokerConfig.PROCESSOR_SIZE; i++) {
            new Processor(requestChannel, i).start();
        }
    }
}
