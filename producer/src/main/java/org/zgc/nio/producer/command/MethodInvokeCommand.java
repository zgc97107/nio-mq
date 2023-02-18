package org.zgc.nio.producer.command;

import com.google.protobuf.Timestamp;
import org.zgc.nio.producer.thread.Processor;
import org.zgc.nio.protocol.MethodInvokeRequest;

public class MethodInvokeCommand extends AbstractCommand {

    private MethodInvokeRequest request;

    public MethodInvokeCommand(String command) {
        super(command);
        Timestamp timestamp = Timestamp.newBuilder()
                .setSeconds(System.currentTimeMillis() / 1000).build();
        MethodInvokeRequest.Builder builder = MethodInvokeRequest.newBuilder()
                .setRequestId(command.hashCode())
                .setClazz(this.args[1])
                .setMethod(this.args[2])
                .setTime(timestamp);
        for (int i = 3; i < args.length; i++) {
            builder.addArgs(args[i]);
        }
        request = builder.build();
    }

    @Override
    public void execute(Processor client) {
        client.send(request);
    }
}
