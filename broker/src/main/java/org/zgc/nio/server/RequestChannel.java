package org.zgc.nio.server;

import org.zgc.nio.protocol.NetworkReceive;
import org.zgc.nio.protocol.Record;

import java.nio.ByteBuffer;
import java.util.Map;
import java.util.concurrent.*;

public class RequestChannel {
    private BlockingQueue<NetworkReceive> requestQueue;

    private BlockingQueue<NetworkReceive>[] responseQueues;

    public RequestChannel(int processorNum) {
        this.requestQueue = new LinkedBlockingQueue<>();
        this.responseQueues = new LinkedBlockingQueue[processorNum];
        for (int i = 0; i < processorNum; i++) {
            responseQueues[i] = new LinkedBlockingDeque<>();
        }
    }

    public void sendRequest(NetworkReceive request) {
        requestQueue.add(request);
    }

    public void sendResponse(NetworkReceive response) {
        BlockingQueue<NetworkReceive> responseQueue = responseQueues[response.getProcessor()];
        responseQueue.add(response);
    }

    public NetworkReceive receiveRequest(){
        return requestQueue.poll();
    }

    public NetworkReceive receiveResponse(int processor){
        BlockingQueue<NetworkReceive> responseQueue = responseQueues[processor];
        return responseQueue.poll();
    }
}
