package org.zgc.nio.producer.command;

import org.zgc.nio.producer.internals.RecordAccumulator;

import java.util.Deque;
import java.util.LinkedList;

public class CommandExecutor {
    private Deque<AbstractCommand> waitingCommands;
    private RecordAccumulator recordAccumulator;

    public CommandExecutor(RecordAccumulator recordAccumulator) {
        this.recordAccumulator = recordAccumulator;
        this.waitingCommands = new LinkedList<>();
    }

    public void execute(String key, Object value) throws Exception {
        switch (key){
            case CommandKeys.SEND:
                recordAccumulator.append((String) value);
                break;
            default:
                break;
        }
    }

    public void addToWaitingList(AbstractCommand abstractCommand){
        this.waitingCommands.addLast(abstractCommand);
    }

    public void notifyWaitingList(){
        AbstractCommand waiting = this.waitingCommands.pollFirst();
        if (waiting == null) {
            return;
        }
        synchronized (waiting) {
            waiting.notifyAll();
        }
    }
}
