package org.zgc.nio.producer;

public class ProducerConfig {
    public static final String HOST = "127.0.0.1";
    public static final int PORT = 8080;
    public static final long BUFFER_ALLOCATE_MAX_WAIT_TIME = 1000L;
    public static final int RECORD_BATCH_BUFFER_SIZE = 4096;
    public static final int RECORD_BATCH_SEND_FREE_SIZE = 1024;
    public static final long RECORD_BATCH_SEND_MAX_WAIT_TIME = 1000L;
    public static final int BUFFER_POOL_MAX_SIZE = 1024;
    public static final int BUFFER_POOL_MAX_MEMORY = BUFFER_POOL_MAX_SIZE * RECORD_BATCH_SEND_FREE_SIZE;
}
