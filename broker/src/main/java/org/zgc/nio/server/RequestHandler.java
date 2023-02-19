package org.zgc.nio.server;

import org.zgc.nio.protocol.NetworkReceive;

public class RequestHandler implements Runnable {
    private final RequestChannel requestChannel;
    private final ReplicaManager replicaManager;

    public RequestHandler(RequestChannel requestChannel, ReplicaManager replicaManager) {
        this.requestChannel = requestChannel;
        this.replicaManager = replicaManager;
    }

    @Override
    public void run() {
        while (true) {
            NetworkReceive request = this.requestChannel.receiveRequest();
            replicaManager.appendMessages(request.getBuffer());
            this.requestChannel.sendResponse(NetworkReceive.response(request));
        }
    }
}
