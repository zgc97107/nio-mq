package org.zgc.nio.producer.command;

import org.zgc.nio.producer.thread.Processor;

public interface Command {
    void execute(Processor client);
}
