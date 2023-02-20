package org.zgc.nio.producer.command;

import com.google.protobuf.Timestamp;
import org.zgc.nio.producer.internals.RecordAccumulator;
import org.zgc.nio.producer.thread.Sender;
import org.zgc.nio.protocol.Record;

import java.util.LinkedList;
import java.util.List;

public class SendMessageCommand extends AbstractCommand {

    private final List<String> messages;

    public SendMessageCommand(String command) {
        super(command);
        this.messages = new LinkedList<>();
    }

    @Override
    public void execute(CommandExecutor executor) {
        messages.forEach(message -> {
            try {
                executor.execute(CommandKeys.SEND, message);
            } catch (Exception e) {
                System.out.println("execute failed exception: " + e);
            }
        });
    }
}
