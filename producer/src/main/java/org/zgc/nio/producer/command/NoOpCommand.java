package org.zgc.nio.producer.command;

import org.zgc.nio.producer.thread.Processor;

public class NoOpCommand implements Command {

    @Override
    public void execute(Processor client) {

    }
}
