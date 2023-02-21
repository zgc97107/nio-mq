package org.zgc.nio.server;

import lombok.extern.slf4j.Slf4j;
import org.zgc.nio.protocol.NetworkReceive;

@Slf4j
public class RequestHandler implements Runnable {
    private final RequestChannel requestChannel;
    private final ReplicaManager replicaManager;

    public RequestHandler(RequestChannel requestChannel, ReplicaManager replicaManager) {
        this.requestChannel = requestChannel;
        this.replicaManager = replicaManager;
    }

    @Override
    public void run() {
        log.info("RequestHandler thread started successfully");
        while (true) {
            NetworkReceive request = this.requestChannel.receiveRequest();
            if (request == null) {
                continue;
            }
            if (request.getBuffer() == null) {
                continue;
            }
            replicaManager.appendMessages(request.getBuffer());
            requestChannel.sendResponse(NetworkReceive.response(request));
        }
    }
}
