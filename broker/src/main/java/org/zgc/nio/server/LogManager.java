package org.zgc.nio.server;

import java.util.concurrent.atomic.AtomicInteger;

public class LogManager{
    private AtomicInteger logDirCounter = new AtomicInteger(0);

    public String nextLogDir() {
        return BrokerConfig.LOG_DIR + "_consumer_offsets-" + logDirCounter;
    }
}
