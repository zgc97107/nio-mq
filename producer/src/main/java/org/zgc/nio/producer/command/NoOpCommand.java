package org.zgc.nio.producer.command;

import org.zgc.nio.producer.thread.Sender;

public class NoOpCommand implements Command {

    @Override
    public void execute(Sender client) {

    }
}
