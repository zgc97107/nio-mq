package org.zgc.nio.server;

import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class LogManager {
    private AtomicInteger logDirCounter = new AtomicInteger(0);

    public String nextLogDir() {
        return BrokerConfig.LOG_DIR + "/TEST-TOPIC-" + logDirCounter + "/";
    }

    public LogSegment logSegment() {
        String logDir = nextLogDir();
        File file = new File(logDir);
        if (!file.exists()) {
            log.info("create log dir: " + logDir);
            file.mkdirs();
        }
        long baseOffset = 0;
        FileMessageSet fileMessageSet = new FileMessageSet(logDir + BrokerConfig.LOG_FILE);
        OffsetIndex offsetIndex = new OffsetIndex(logDir + BrokerConfig.LOG_INDEX_FILE, baseOffset);
        LogSegment logSegment = new LogSegment(offsetIndex, fileMessageSet, 0, BrokerConfig.INDEX_INTERVAL_BYTES);
        log.info("load log segment successfully, baseOffset: " + baseOffset);
        return logSegment;
    }
}
