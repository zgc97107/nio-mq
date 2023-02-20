package org.zgc.nio.producer.command;

import org.zgc.nio.producer.thread.Sender;

public interface Command {
    void execute(CommandExecutor executor);
}
