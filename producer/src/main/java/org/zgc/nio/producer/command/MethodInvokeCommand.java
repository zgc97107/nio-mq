package org.zgc.nio.producer.command;

import com.google.protobuf.Timestamp;
import org.zgc.nio.producer.thread.Sender;
import org.zgc.nio.protocol.Record;

public class MethodInvokeCommand extends AbstractCommand {

    private Record request;

    public MethodInvokeCommand(String command) {
        super(command);
        Timestamp timestamp = Timestamp.newBuilder()
                .setSeconds(System.currentTimeMillis() / 1000).build();
        Record.Builder builder = Record.newBuilder()
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
    public void execute(Sender client) {
        client.send(request);
    }
}
