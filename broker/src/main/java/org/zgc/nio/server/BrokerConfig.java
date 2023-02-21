package org.zgc.nio.server;

public class BrokerConfig {

    public static final int PORT = 8848;
    public static final int PROCESSOR_SIZE = 4;
    public static final int INDEX_FILE_SIZE = 1024;
    public static final int INDEX_INTERVAL_BYTES = 1024;
    public static final String LOG_DIR = "C:/tools/workspace/nio-mq/log";

    public static final String LOG_INDEX_FILE = "00000.index";
    public static final String LOG_FILE = "00000.data";
}
